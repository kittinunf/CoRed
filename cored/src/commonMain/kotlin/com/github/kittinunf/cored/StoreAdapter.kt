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
): StoreType<S> {
    return StoreAdapter(Store(scope, initialState, StoreAdapterEngine((reducers + SetStateReducerType()).toMutableMap(), mutableMapOf())))
}

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<KClass<out Any>, Reducer<S, A>>,
    middlewares: Map<KClass<out Any>, Middleware<S, A>>
): StoreType<S> {
    return StoreAdapter(Store(scope, initialState, StoreAdapterEngine((reducers + SetStateReducerType()).toMutableMap(), middlewares.toMutableMap())))
}

private class StoreAdapterEngine<S : Any, A : Any>(
    val reducerMap: MutableMap<KClass<out Any>, Reducer<S, A>>,
    val middlewareMap: MutableMap<KClass<out Any>, Middleware<S, A>>
) : StateScannerEngine<S> {

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

    override fun addMiddleware(action: Any, middleware: AnyMiddleware<S>) {
        middlewareMap.put(action::class, middleware)
    }

    override fun removeMiddleware(action: Any, middleware: AnyMiddleware<S>): Boolean {
        return middlewareMap.remove(action::class) != null
    }
}

private class StoreAdapter<S : Any>(private val store: Store<S>) : StoreType<S> by store