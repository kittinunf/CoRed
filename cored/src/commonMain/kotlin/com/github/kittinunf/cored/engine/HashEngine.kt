package com.github.kittinunf.cored.engine

import com.github.kittinunf.cored.ActionMiddleware
import com.github.kittinunf.cored.ActionReducer
import com.github.kittinunf.cored.AnyMiddleware
import com.github.kittinunf.cored.AnyReducer
import com.github.kittinunf.cored.Middleware
import com.github.kittinunf.cored.Order
import com.github.kittinunf.cored.Reducer
import com.github.kittinunf.cored.combineReducers
import com.github.kittinunf.cored.store.Store
import kotlin.reflect.KClass

internal class HashEngine<S : Any, A : Any>(
    val reducerMap: MutableMap<KClass<out Any>, Reducer<S, A>>,
    val middlewareMap: MutableMap<KClass<out Any>, Middleware<S, A>>
) : Engine<S> {

    override suspend fun scan(store: Store<S>, state: S, action: Any): S {
        val identifier = action::class
        // if reducer is not found, we do nothing with our state
        val reducer = reducerMap[identifier] ?: return state

        val middleware = middlewareMap[identifier]
        val typedAction = action as? A
        return if (typedAction == null) state else {
            middleware?.invoke(Order.BeforeReduce, store, state, typedAction)
            val nextState = reducer.invoke(state, typedAction)
            middleware?.invoke(Order.AfterReduce, store, nextState, typedAction)
            nextState
        }
    }

    override val reducer: AnyReducer<S> =
        combineReducers(reducerMap.values.toList() as List<AnyReducer<S>>)

    override val middlewares: MutableList<AnyMiddleware<S>>
        get() = middlewareMap.values.toMutableList() as MutableList<AnyMiddleware<S>>

    override fun addMiddleware(actionMiddleware: ActionMiddleware<S, Any>) {
        val (key, middleware) = actionMiddleware
        middlewareMap.put(key, middleware)
    }

    override fun removeMiddleware(actionMiddleware: ActionMiddleware<S, Any>): Boolean {
        val (key, _) = actionMiddleware
        return middlewareMap.remove(key) != null
    }

    override fun addMiddleware(middleware: AnyMiddleware<S>) {
        error("This engine does not support this addMiddleware without key, please use fun addMiddleware(key: Any, middleware: AnyMiddleware<S>) instead")
    }

    override fun removeMiddleware(middleware: AnyMiddleware<S>): Boolean {
        error("This engine does not support this removeMiddleware without key, please use fun removeMiddleware(key: Any, middleware: AnyMiddleware<S>) instead")
    }

    override fun addReducer(actionReducer: ActionReducer<S, Any>) {
        val (key, reducer) = actionReducer
        reducerMap.put(key, reducer)
    }

    override fun removeReducer(actionReducer: ActionReducer<S, Any>): Boolean {
        val (key, _) = actionReducer
        return reducerMap.remove(key) != null
    }
}
