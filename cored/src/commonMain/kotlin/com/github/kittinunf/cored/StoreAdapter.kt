package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

interface Identifiable {

    val identifier: String
}

typealias ReducerType<S, A> = Pair<String, Reducer<S, A>>
typealias EffectType<S, A> = Pair<String, Middleware<S, A>>

private object SetStateActionIdentifiable : Identifiable {
    // prepend with 2 underscores so it won't collide with the client identifier string
    override val identifier: String = "__SetState"
}

@Suppress("FunctionName")
private fun <S : Any> SetStateReducerType(): ReducerType<S, Any> = SetStateActionIdentifiable.identifier to SetStateReducer()

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<String, Reducer<S, A>>
): StoreType<S> {
    return StoreAdapter(Store(scope, initialState, StoreAdapterEngine((reducers + SetStateReducerType()).toMutableMap(), mutableMapOf())))
}

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<String, Reducer<S, A>>,
    middlewares: Map<String, Middleware<S, A>>
): StoreType<S> {
    return StoreAdapter(Store(scope, initialState, StoreAdapterEngine((reducers + SetStateReducerType()).toMutableMap(), middlewares.toMutableMap())))
}

private class StoreAdapterEngine<S : Any, A : Any>(
    val reducerMap: MutableMap<String, Reducer<S, A>>,
    val middlewareMap: MutableMap<String, Middleware<S, A>>
) : StateScannerEngine<S> {

    override suspend fun scan(storeType: StoreType<S>, state: S, action: Any): S {
        // check whether it is identifiable or it is a SetStateAction
        val id = if (action is SetStateAction<*>) SetStateActionIdentifiable else action as? Identifiable
        val identifier = id?.identifier ?: action::class.simpleName!!

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
        get() = TODO("Not yet implemented")
}

private class StoreAdapter<S : Any>(private val store: Store<S>) : StoreType<S> by store {

    override fun addMiddleware(middleware: AnyMiddleware<S>) = error("Not supported yet")

    override fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean = error("Not supported yet")
}
