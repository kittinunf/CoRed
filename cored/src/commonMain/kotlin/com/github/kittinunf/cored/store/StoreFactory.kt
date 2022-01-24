package com.github.kittinunf.cored.store

import com.github.kittinunf.cored.AnyMiddleware
import com.github.kittinunf.cored.AnyReducer
import com.github.kittinunf.cored.ActionMiddleware
import com.github.kittinunf.cored.Middleware
import com.github.kittinunf.cored.Reducer
import com.github.kittinunf.cored.ActionReducer
import com.github.kittinunf.cored.SetStateReducer
import com.github.kittinunf.cored.SetStateActionReducer
import com.github.kittinunf.cored.combineReducers
import com.github.kittinunf.cored.engine.IteratorEngine
import com.github.kittinunf.cored.engine.HashEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.reflect.KClass

@Suppress("FunctionName")
fun <S : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducer: AnyReducer<S>,
): ReduxStore<S> = ReduxStore(
    scope = scope,
    initialState = initialState,
    engine = IteratorEngine(
        reducer = combineReducers(reducer, SetStateReducer()),
        middlewares = mutableListOf()
    )
)

@Suppress("FunctionName")
fun <S : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducer: AnyReducer<S>,
    middleware: AnyMiddleware<S>
): ReduxStore<S> {
    return ReduxStore(
        scope = scope,
        initialState = initialState,
        engine = IteratorEngine(
            reducer = combineReducers(reducer, SetStateReducer()),
            middlewares = mutableListOf(middleware)
        )
    )
}

@Suppress("FunctionName")
fun <S : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducer: AnyReducer<S>,
    vararg middlewares: AnyMiddleware<S>
): ReduxStore<S> {
    return ReduxStore(
        scope = scope,
        initialState = initialState,
        engine = IteratorEngine(
            reducer = combineReducers(reducer, SetStateReducer()),
            middlewares = middlewares.toMutableList()
        )
    )
}

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<KClass<out Any>, Reducer<S, A>>
): ReduxStore<S> = ReduxStore(
    scope = scope,
    initialState = initialState,
    engine = HashEngine(
        reducerMap = (reducers + SetStateActionReducer()).toMutableMap(),
        middlewareMap = mutableMapOf()
    )
)

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Set<ActionReducer<S, A>>
): ReduxStore<S> {
    val map = reducers.associate { it }
    return ReduxStore(
        scope = scope,
        initialState = initialState,
        engine = HashEngine(
            reducerMap = (map + SetStateActionReducer()).toMutableMap(),
            middlewareMap = mutableMapOf()
        )
    )
}

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Map<KClass<out Any>, Reducer<S, A>>,
    middlewares: Map<KClass<out Any>, Middleware<S, A>>
): ReduxStore<S> = ReduxStore(
    scope = scope,
    initialState = initialState,
    engine = HashEngine(
        reducerMap = (reducers + SetStateActionReducer()).toMutableMap(),
        middlewareMap = middlewares.toMutableMap()
    )
)

@Suppress("FunctionName")
fun <S : Any, A : Any> Store(
    scope: CoroutineScope = GlobalScope,
    initialState: S,
    reducers: Set<ActionReducer<S, A>>,
    middlewares: Set<ActionMiddleware<S, A>>
): ReduxStore<S> {
    val reducerMap = reducers.associate { it }
    val middlewareMap = middlewares.associate { it }
    return ReduxStore(
        scope = scope,
        initialState = initialState,
        engine = HashEngine(
            reducerMap = (reducerMap + SetStateActionReducer()).toMutableMap(),
            middlewareMap = middlewareMap.toMutableMap()
        )
    )
}
