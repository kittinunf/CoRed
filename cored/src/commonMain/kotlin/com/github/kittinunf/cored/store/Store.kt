package com.github.kittinunf.cored.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Store<S : Any> {

    val states: StateFlow<S>

    val currentState: S

    suspend fun dispatch(action: Any)

    fun tryDispatch(action: Any): Boolean

    suspend fun dispatch(actions: Flow<Any>)

    fun trySetState(stateProducer: () -> S): Boolean

    suspend fun setState(stateProducer: () -> S)
}
