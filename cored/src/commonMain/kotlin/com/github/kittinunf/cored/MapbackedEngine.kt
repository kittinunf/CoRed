package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.reflect.KClass

typealias ReducerType<S, A> = Pair<KClass<out Any>, Reducer<S, A>>
typealias EffectType<S, A> = Pair<KClass<out Any>, Middleware<S, A>>

@Suppress("FunctionName")
private fun <S : Any> SetStateReducerType(): ReducerType<S, Any> = SetStateAction::class to SetStateReducer()

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<KClass<out Any>, Reducer<S, A>>
): Store<S> {
    return Store(scope, initialState, MapBackedEngine((reducers + SetStateReducerType()).toMutableMap(), mutableMapOf()))
}

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Set<ReducerType<S, A>>
): Store<S> {
    val map = reducers.associate { it }
    return Store(scope, initialState, MapBackedEngine((map + SetStateReducerType()).toMutableMap(), mutableMapOf()))
}

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<KClass<out Any>, Reducer<S, A>>,
    middlewares: Map<KClass<out Any>, Middleware<S, A>>
): Store<S> {
    return Store(scope, initialState, MapBackedEngine((reducers + SetStateReducerType()).toMutableMap(), middlewares.toMutableMap()))
}

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Set<ReducerType<S, A>>,
    middlewares: Set<EffectType<S, A>>
): Store<S> {
    val reducerMap = reducers.associate { it }
    val middlewareMap = middlewares.associate { it }
    return Store(scope, initialState, MapBackedEngine((reducerMap + SetStateReducerType()).toMutableMap(), middlewareMap.toMutableMap()))
}

inline fun <reified A : Any, S : Any> reducer(reducer: Reducer<S, A>): ReducerType<S, A> = A::class to reducer

private class MapBackedEngine<S : Any, A : Any>(
    val reducerMap: MutableMap<KClass<out Any>, Reducer<S, A>>,
    val middlewareMap: MutableMap<KClass<out Any>, Middleware<S, A>>
) : Engine<S> {

    override suspend fun scan(storeType: StoreType<S>, state: S, action: Any): S {
        val identifier = action::class

        val middleware = middlewareMap[identifier]
        val reducer = reducerMap.getValue(identifier)

        val typedAction = action as? A

        return if (typedAction == null) state else {
            middleware?.invoke(Order.BeforeReduce, storeType, state, typedAction)
            val nextState = reducer(state, typedAction)
            middleware?.invoke(Order.AfterReduced, storeType, nextState, typedAction)
            nextState
        }
    }

    override val reducer: AnyReducer<S> = combineReducers(reducerMap.values.toList() as List<AnyReducer<S>>)

    override val middlewares: MutableList<AnyMiddleware<S>>
        get() = middlewareMap.values.toMutableList() as MutableList<AnyMiddleware<S>>

    override fun addMiddleware(key: Any, middleware: AnyMiddleware<S>) {
        middlewareMap.put(key as KClass<out Any>, middleware)
    }

    override fun removeMiddleware(key: Any, middleware: AnyMiddleware<S>): Boolean {
        return middlewareMap.remove(key as KClass<out Any>) != null
    }

    override fun addMiddleware(middleware: AnyMiddleware<S>) {
        error("This engine does not support this addMiddleware without key, please use fun addMiddleware(key: Any, middleware: AnyMiddleware<S>) instead")
    }

    override fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean {
        error("This engine does not support this removeMiddleware without key, please use fun removeMiddleware(key: Any, middleware: AnyMiddleware<S>) instead")
    }
}
