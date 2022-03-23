package com.github.kittinunf.cored.store

import com.github.kittinunf.cored.SetStateAction
import com.github.kittinunf.cored.engine.Engine
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan

class ReduxStore<S : Any> internal constructor(initialState: S, private val engine: Engine<S>) : Store<S>, Engine<S> by engine {

    companion object {
        const val defaultBufferCapacity = 16
    }

    // seed action
    private object NoAction

    private val _actions = MutableSharedFlow<Any>(
        replay = 0,
        extraBufferCapacity = defaultBufferCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override var currentState: S = initialState
        private set

    override val states: Flow<S> = _actions
        .scan(initialState to NoAction as Any) { (state, _), action ->
            val nextState = scan(this, state, action)
            nextState to action
        }
        .map { (state, _) -> state }
        .onEach { currentState = it }

    override suspend fun setState(stateProducer: () -> S) {
        val newState = stateProducer()
        _actions.emit(SetStateAction(newState))
    }

    override fun trySetState(stateProducer: () -> S): Boolean {
        val newState = stateProducer()
        return _actions.tryEmit(SetStateAction(newState))
    }

    override suspend fun dispatch(action: Any) {
        _actions.emit(action)
    }

    override suspend fun dispatch(actions: Flow<Any>) {
        actions.collect(_actions::emit)
    }

    override fun tryDispatch(action: Any): Boolean = _actions.tryEmit(action)
}
