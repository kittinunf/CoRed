package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

typealias AnyReducer<S> = Reducer<S, Any>

fun interface Reducer<S : Any, in A : Any> {

    operator fun invoke(currentState: S, action: A): S
}

class NoopReducer<S> : AnyReducer<S> {

    override operator fun invoke(currentState: S, action: Any): S = currentState
}

enum class Order {
    BeforeReduce,
    AfterReduced
}

typealias AnyMiddleware<S> = Middleware<S, Any>

fun interface Middleware<S : Any, in A : Any> {

    operator fun invoke(order: Order, store: StoreType<S>, state: S, action: A)
}

interface StoreType<S : Any> {

    val states: StateFlow<S>

    val currentState: S

    suspend fun dispatch(action: Any)

    fun tryDispatch(action: Any): Boolean

    suspend fun dispatch(actions: Flow<Any>)

    fun trySetState(stateProducer: () -> S): Boolean

    suspend fun setState(stateProducer: () -> S)
}

internal class SetStateAction<S : Any>(val newState: S)

internal class SetStateReducer<S : Any> : AnyReducer<S> {

    override fun invoke(currentState: S, action: Any): S = (action as? SetStateAction<S>)?.newState
        ?: currentState
}

@Suppress("FunctionName")
fun <S : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducer: AnyReducer<S>,
): Store<S> = Store(scope, initialState, DefaultEngine(combineReducers(reducer, SetStateReducer()), mutableListOf()))

@Suppress("FunctionName")
fun <S : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducer: AnyReducer<S>,
    middleware: AnyMiddleware<S>
): Store<S> {
    return Store(scope, initialState, DefaultEngine(combineReducers(reducer, SetStateReducer()), mutableListOf(middleware)))
}

@Suppress("FunctionName")
fun <S : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducer: AnyReducer<S>,
    vararg middlewares: AnyMiddleware<S>
): Store<S> {
    return Store(scope, initialState, DefaultEngine(combineReducers(reducer, SetStateReducer()), middlewares.toMutableList()))
}

interface Engine<S : Any> {

    val reducer: AnyReducer<S>

    val middlewares: MutableList<AnyMiddleware<S>>

    suspend fun scan(storeType: StoreType<S>, state: S, action: Any): S

    fun addMiddleware(key: Any, middleware: AnyMiddleware<S>) = addMiddleware(middleware)

    fun removeMiddleware(key: Any, middleware: AnyMiddleware<S>): Boolean = removeMiddleware(middleware)

    fun addMiddleware(middleware: AnyMiddleware<S>)

    fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean
}

private class DefaultEngine<S : Any>(override val reducer: AnyReducer<S>, override val middlewares: MutableList<AnyMiddleware<S>>) :
    Engine<S> {

    override suspend fun scan(storeType: StoreType<S>, state: S, action: Any): S {
        middlewares.onEach { it(Order.BeforeReduce, storeType, state, action) }
        val nextState = reducer(state, action)
        middlewares.onEach { it(Order.AfterReduced, storeType, nextState, action) }
        return nextState
    }

    override fun addMiddleware(middleware: AnyMiddleware<S>) {
        middlewares.add(middleware)
    }

    override fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean = middlewares.remove(middleware)
}

class Store<S : Any> internal constructor(scope: CoroutineScope, initialState: S, private val engine: Engine<S>) : StoreType<S>, Engine<S> by engine {

    companion object {
        const val defaultBufferCapacity = 16
    }

    // seed action
    private object NoAction

    private val _actions =
        MutableSharedFlow<Any>(replay = 0, extraBufferCapacity = defaultBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val states: StateFlow<S> = _actions
        .scan(initialState to NoAction as Any) { (state, _), action ->
            val nextState = scan(this, state, action)
            nextState to action
        }
        .map { it.first }
        .stateIn(scope, SharingStarted.Lazily, initialState)

    override val currentState: S
        get() = states.value

    override fun trySetState(stateProducer: () -> S): Boolean {
        val newState = stateProducer()
        return _actions.tryEmit(SetStateAction(newState))
    }

    override suspend fun setState(stateProducer: () -> S) {
        val newState = stateProducer()
        _actions.emit(SetStateAction(newState))
    }

    override suspend fun dispatch(action: Any) {
        _actions.emit(action)
    }

    override fun tryDispatch(action: Any): Boolean = _actions.tryEmit(action)

    override suspend fun dispatch(actions: Flow<Any>) {
        actions.collect(_actions::emit)
    }
}

fun <S : Any> combineReducers(reducers: List<AnyReducer<S>>): AnyReducer<S> = CompositeReducer(reducers)

fun <S : Any> combineReducers(vararg reducers: AnyReducer<S>): AnyReducer<S> = CompositeReducer(reducers.asList())

private class CompositeReducer<S : Any>(private val reducers: List<AnyReducer<S>>) : AnyReducer<S> {

    override operator fun invoke(currentState: S, action: Any): S = reducers.fold(currentState) { state, reducer -> reducer(state, action) }
}
