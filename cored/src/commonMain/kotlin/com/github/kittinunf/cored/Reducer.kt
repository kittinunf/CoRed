package com.github.kittinunf.cored

import kotlin.reflect.KClass

typealias AnyReducer<S> = Reducer<S, Any>
typealias ActionReducer<S, A> = Pair<KClass<out Any>, Reducer<S, A>>

enum class Order {
    BeforeReduce,
    AfterReduce
}

inline fun <reified S : Any, reified A : Any> reducer(reducer: Reducer<S, A>): ActionReducer<S, A> =
    A::class to reducer

fun interface Reducer<S : Any, in A : Any> {
    operator fun invoke(currentState: S, action: A): S
}

class NoopReducer<S> : AnyReducer<S> {
    override operator fun invoke(currentState: S, action: Any): S = currentState
}

fun <S : Any> combineReducers(reducers: List<AnyReducer<S>>): AnyReducer<S> =
    CompositeReducer(reducers)

fun <S : Any> combineReducers(vararg reducers: AnyReducer<S>): AnyReducer<S> =
    CompositeReducer(reducers.asList())

private class CompositeReducer<S : Any>(private val reducers: List<AnyReducer<S>>) : AnyReducer<S> {
    override operator fun invoke(currentState: S, action: Any): S =
        reducers.fold(currentState) { state, reducer -> reducer(state, action) }
}

internal class SetStateAction<S : Any>(val newState: S)

internal class SetStateReducer<S : Any> : AnyReducer<S> {
    override fun invoke(currentState: S, action: Any): S = (action as? SetStateAction<S>)?.newState ?: currentState
}

@Suppress("FunctionName")
internal fun <S : Any> SetStateActionReducer(): ActionReducer<S, Any> =
    SetStateAction::class to SetStateReducer()
