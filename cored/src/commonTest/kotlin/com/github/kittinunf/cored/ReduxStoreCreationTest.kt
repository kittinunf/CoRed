package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReduxStoreCreationTest {
    private var counterState = CounterState()

    data class SideEffectData(var value: Int)
    private val sideEffectData = SideEffectData(10)

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private val store = Store(testScope, counterState,
        setOf(
            reducerType { currentState: CounterState, action: Increment ->
                currentState.copy(counter = currentState.counter + action.by)
            },
            reducerType { currentState: CounterState, action: Decrement ->
                currentState.copy(counter = currentState.counter - action.by)
            }
        ),
        setOf(
            effectType { order, store, state, action: Increment ->
                if (order == Order.AfterReduced) {
                    sideEffectData.value = sideEffectData.value + state.counter
                }
            },
            effectType { order, store, state, action: Decrement ->
                if (order == Order.AfterReduced) {
                    sideEffectData.value = sideEffectData.value - state.counter
                }
            }
        )
    )

    @BeforeTest
    fun before() {
        counterState = CounterState()
        sideEffectData.value = 10
    }

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

    @Test
    fun `should update invoke middleware to update sideEffectData`() {
        runTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> assertEquals(0, state.counter)
                        1 -> {
                            assertEquals(1, state.counter)
                        }
                        2 -> {
                            assertEquals(3, state.counter)
                        }
                        3 -> {
                            assertEquals(-7, state.counter)
                        }
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(1))
            store.dispatch(Increment(2))
            store.dispatch(Decrement(10))

            assertEquals(21, sideEffectData.value) //10 + 0 + 1 + 3 - (-7)
        }
    }
}
