package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReduxStoreCreationTest {
    private val counterState = CounterState()

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private val store = Store(testScope, counterState, setOf(
        reducerType(Reducer { currentState: CounterState, action: Increment ->
            currentState.copy(counter = currentState.counter + action.by)
        }),
        reducerType(Reducer { currentState: CounterState, action: Decrement ->
            currentState.copy(counter = currentState.counter - action.by)
        })
    ))

    @Test
    fun `should increment state`() {
        runTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> assertEquals(0, state.counter)
                        1 -> assertEquals(1, state.counter)
                        2 -> assertEquals(3, state.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(1))
            store.dispatch(Increment(2))
        }
    }
}
