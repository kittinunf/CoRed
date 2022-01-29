package com.github.kittinunf.cored.engine

import com.github.kittinunf.cored.AnyMiddleware
import com.github.kittinunf.cored.AnyReducer
import com.github.kittinunf.cored.store.Store

internal interface Engine<S : Any> {

    val reducer: AnyReducer<S>

    val middlewares: MutableList<AnyMiddleware<S>>

    suspend fun scan(store: Store<S>, state: S, action: Any): S

    fun addMiddleware(key: Any, middleware: AnyMiddleware<S>) {
        addMiddleware(middleware)
    }

    fun removeMiddleware(key: Any, middleware: AnyMiddleware<S>): Boolean =
        removeMiddleware(middleware)

    fun addMiddleware(middleware: AnyMiddleware<S>)

    fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean

    fun addReducer(key: Any, reducer: AnyReducer<S>)

    fun removeReducer(key: Any, reducer: AnyReducer<S>): Boolean
}
