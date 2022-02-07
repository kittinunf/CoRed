package com.github.kittinunf.cored.engine

import com.github.kittinunf.cored.AnyMiddleware
import com.github.kittinunf.cored.AnyReducer
import com.github.kittinunf.cored.Order
import com.github.kittinunf.cored.store.Store

internal class IteratorEngine<S : Any>(
    override val reducer: AnyReducer<S>,
    override val middlewares: MutableList<AnyMiddleware<S>>
) : Engine<S> {

    override suspend fun scan(store: Store<S>, state: S, action: Any): S {
        middlewares.onEach { it(Order.BeforeReduce, store, state, action) }
        val nextState = reducer(state, action)
        middlewares.onEach { it(Order.AfterReduce, store, nextState, action) }
        return nextState
    }

    override fun addMiddleware(middleware: AnyMiddleware<S>) {
        middlewares.add(middleware)
    }

    override fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean =
        middlewares.remove(middleware)

    override fun addReducer(key: Any, reducer: AnyReducer<S>) {
        error("This engine does not support this addReducer")
    }

    override fun removeReducer(key: Any): Boolean {
        error("This engine does not support this removeReducer")
    }
}
