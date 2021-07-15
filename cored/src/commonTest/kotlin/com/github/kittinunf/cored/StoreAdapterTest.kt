package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StoreAdapterTest {

    private val counterState = CounterState()

    private val testScope = CoroutineScope(Dispatchers.Unconfined)
    private val reducers = mapOf(
        Increment::class to Reducer { currentState: CounterState, action: Increment ->
            currentState.copy(counter = currentState.counter + action.by)
        },
        Decrement::class to Reducer { currentState: CounterState, action: Decrement ->
            currentState.copy(counter = currentState.counter - action.by)
        }
    )

    private val store = Store(testScope, counterState, reducers)

    @Test
    fun `should increment state`() {
        runBlockingTest {
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
    fun `should decrement state`() {
        runBlockingTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> assertEquals(0, state.counter)
                        1 -> assertEquals(-2, state.counter)
                        2 -> assertEquals(-5, state.counter)
                        3 -> assertEquals(-10, state.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Decrement(2))
            store.dispatch(Decrement(3))
            store.dispatch(Decrement(5))
        }
    }

    @Test
    fun `should emit initial value`() {
        runBlockingTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    assertEquals(0, index)
                    assertEquals(0, state.counter)
                }
                .printDebug()
                .launchIn(testScope)
        }
    }

    @Test
    fun `should not emit value if the state not changed`() {
        runBlockingTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> assertEquals(0, state.counter)
                        else -> fail("should not reach here")
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(0))
            store.dispatch(Decrement(0))
        }
    }

    @Test
    fun `should not emit same value up until the same state is emitted`() {
        runBlockingTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> assertEquals(0, state.counter)
                        1 -> assertEquals(1, state.counter)
                        else -> fail("should not reach here")
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(1))
            store.dispatch(Decrement(0))
            store.dispatch(Decrement(0))
        }
    }

    @Test
    fun `should dispatch multiple value from Flow emitter block`() {
        runBlockingTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        4 -> assertEquals(0, state.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(flow {
                emit(Increment(10))
                emit(Decrement(10))
                emit(Increment(100))
                emit(Decrement(100))
            })
        }
    }

    @Test
    fun `should invoke middleware if one is being set`() {
        data class SideEffectData(var value: Int)

        val sideEffectData = SideEffectData(100)

        val localStore = Store(testScope, CounterState(), reducers, mapOf(
            Increment::class to Middleware { order: Order, store: CounterStore, state: CounterState, action: Increment ->
                if (order == Order.BeforeReduce) {
                    assertEquals(0, state.counter)
                } else {
                    sideEffectData.value = sideEffectData.value + state.counter
                }
            }
        ))

        runBlockingTest {
            localStore.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Increment(100))
        }

        assertEquals(200, sideEffectData.value)
    }

    @Test
    @Ignore
    fun `should invoke middleware until remove`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `should invoke middleware in the correct order`() {
        val localStore = Store(testScope, CounterState(), reducers, mapOf(
            Increment::class to Middleware { order: Order, store: CounterStore, state: CounterState, action: Increment ->
                if (order == Order.BeforeReduce) {
                    assertEquals(0, state.counter)
                } else {
                    assertEquals(100, state.counter)
                }
            }
        ))

        runBlockingTest {
            localStore.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Increment(100))
        }
    }

    @Test
    fun `should invoke even we don't provide the customization on the identifier with the qualified name`() {
        val localStore = Store(testScope, CounterState(), mapOf(
            Set::class to Reducer { currentState: CounterState, action: Set ->
                currentState.copy(counter = action.value)
            }
        ))

        runBlockingTest {
            localStore.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Set(500))
        }

        assertEquals(500, localStore.currentState.counter)
    }

    @Test
    fun `should be able to dispatch action from the middleware`() {
        val localStore = Store(testScope, CounterState(), reducers, mapOf(
            Increment::class to Middleware { order: Order, store: CounterStore, state: CounterState, action: Increment ->
                if (order == Order.AfterReduced) {
                    if (state.counter == 100) {
                        // dispatch another action from middleware
                        runBlockingTest {
                            delay(1000)
                            store.dispatch(Increment(10))
                        }
                        store.tryDispatch(Decrement(200))
                    }
                }
            }
        ))

        runBlockingTest {
            localStore.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Increment(100))
        }

        assertEquals(-90, localStore.currentState.counter)
    }

    @Test
    fun `should be able to support setStateReducer by try setting new state directly to the store`() {
        runBlockingTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        1 -> assertEquals(100, state.counter)
                        2 -> assertEquals(99, state.counter)
                        3 -> assertEquals(1000, state.counter)
                        4 -> assertEquals(900, state.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(100))
            store.dispatch(Decrement(1))

            store.trySetState {
                CounterState(1000)
            }

            store.dispatch(Decrement(100))
        }
    }

    @Test
    fun `should be able to support setStateReducer by setting new state directly to the store`() {
        runBlockingTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        1 -> assertEquals(100, state.counter)
                        2 -> assertEquals(99, state.counter)
                        3 -> assertEquals(1000, state.counter)
                        4 -> assertEquals(900, state.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(100))
            store.dispatch(Decrement(1))

            store.setState {
                CounterState(1000)
            }

            store.dispatch(Decrement(100))
        }
    }
}
