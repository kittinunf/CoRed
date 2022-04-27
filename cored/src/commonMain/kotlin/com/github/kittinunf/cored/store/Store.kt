package com.github.kittinunf.cored.store

import kotlinx.coroutines.flow.Flow

interface Store<S : Any> {

    val states: Flow<S>

    val currentState: S

    fun dispatch(action: Any): Boolean

    suspend fun dispatch(actions: Flow<Any>)

    fun setState(stateProducer: () -> S): Boolean
}
