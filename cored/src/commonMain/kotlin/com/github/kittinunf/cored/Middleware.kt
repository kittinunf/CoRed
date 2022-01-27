package com.github.kittinunf.cored

import com.github.kittinunf.cored.store.Store
import kotlin.reflect.KClass

typealias AnyMiddleware<S> = Middleware<S, Any>
typealias ActionMiddleware<S, A> = Pair<KClass<out Any>, Middleware<S, A>>

inline fun <reified A : Any, S : Any> middleware(middleware: Middleware<S, A>): ActionMiddleware<S, A> =
    A::class to middleware

fun interface Middleware<S : Any, in A : Any> {
    operator fun invoke(order: Order, store: Store<S>, state: S, action: A)
}
