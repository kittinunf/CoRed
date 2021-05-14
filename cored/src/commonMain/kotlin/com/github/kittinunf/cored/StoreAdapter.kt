package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

interface Identifiable {

    val identifier: String
        get() = this::class.simpleName!!
}

typealias ReducerType<S, A> = Pair<String, Reducer<S, A>>
typealias EffectType<S, A> = Pair<String, Middleware<S, A>>

fun <S : State, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<String, Reducer<S, A>>
): StoreType<S> {
    return StoreAdapter(Store(scope, initialState, StoreAdapterEngine(reducers.toMutableMap(), mutableMapOf())))
}

fun <S : State, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<String, Reducer<S, A>>,
    middlewares: Map<String, Middleware<S, A>>
): StoreType<S> {
    return StoreAdapter(Store(scope, initialState, StoreAdapterEngine(reducers.toMutableMap(), middlewares.toMutableMap())))
}

private class StoreAdapterEngine<S : State, A : Any>(
    val reducerMap: MutableMap<String, Reducer<S, A>>,
    val middlewareMap: MutableMap<String, Middleware<S, A>>
) : StateScannerEngine<S> {

    override suspend fun scan(storeType: StoreType<S>, state: S, action: Any): S {
        val id = checkNotNull(action as? Identifiable)

        val middleware = middlewareMap[id.identifier]
        val reducer = reducerMap.getValue(id.identifier)

        val typedAction = action as? A

        return if (typedAction == null) state else {
            middleware?.process(Order.BeforeReduce, storeType, state, typedAction)
            val nextState = reducer(state, typedAction)
            middleware?.process(Order.AfterReduced, storeType, nextState, typedAction)
            nextState
        }
    }

    override val reducer: AnyReducer<S> = combineReducers(reducerMap.values.toList() as List<AnyReducer<S>>)

    override val middlewares: MutableList<AnyMiddleware<S>>
        get() = TODO("Not yet implemented")
}

private class StoreAdapter<S : State>(private val store: Store<S>) : StoreType<S> by store {

    override fun addMiddleware(middleware: AnyMiddleware<S>) = error("Not supported yet")

    override fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean = error("Not supported yet")
}
