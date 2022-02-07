package com.github.kittinunf.cored.engine

import com.github.kittinunf.cored.ActionMiddleware
import com.github.kittinunf.cored.ActionReducer
import com.github.kittinunf.cored.AnyMiddleware
import com.github.kittinunf.cored.AnyReducer
import com.github.kittinunf.cored.store.Store

internal interface Engine<S : Any> {

    val reducer: AnyReducer<S>

    val middlewares: MutableList<AnyMiddleware<S>>

    suspend fun scan(store: Store<S>, state: S, action: Any): S

    fun addMiddleware(actionMiddleware: ActionMiddleware<S, Any>) {
        val (_, middleware) = actionMiddleware
        addMiddleware(middleware)
    }

    fun removeMiddleware(actionMiddleware: ActionMiddleware<S, Any>): Boolean {
        val (_, middleware) = actionMiddleware
        return removeMiddleware(middleware)
    }

    fun addMiddleware(middleware: AnyMiddleware<S>)

    fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean

    fun addReducer(actionReducer: ActionReducer<S, Any>)

    fun removeReducer(key: Any): Boolean
}
