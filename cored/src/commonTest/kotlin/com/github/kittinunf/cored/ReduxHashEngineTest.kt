package com.github.kittinunf.cored

import com.github.kittinunf.cored.store.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ReduxHashEngineTest {

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

    private val store = Store(counterState, reducers)

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
    fun `should decrement state`() {
        runTest {
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
        runTest {
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
    fun `should not emit value if the state not changed with stateIn()`() {
        runTest {
            store.states
                .stateIn(testScope)
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
    fun `should not emit same value up until the same state is emitted with stateIn()`() {
        runTest {
            store.states
                .stateIn(testScope)
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
        runTest {
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

        val localStore =
            Store(CounterState(), reducers, mapOf(
                Increment::class to Middleware { order: Order, _: CounterStore, state: CounterState, _: Increment ->
                    if (order == Order.BeforeReduce) {
                        assertEquals(0, state.counter)
                    } else {
                        sideEffectData.value = sideEffectData.value + state.counter
                    }
                }
            ))

        localStore.addMiddleware(Decrement::class to Middleware { order: Order, _: Store<CounterState>, state: CounterState, _: Decrement ->
            if (order == Order.AfterReduce) {
                sideEffectData.value = sideEffectData.value - state.counter
            }
        } as AnyMiddleware<CounterState>)

        runTest {
            localStore.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Increment(100))
        }

        assertEquals(200, sideEffectData.value)

        // Test after add new middleware
        runTest {
            localStore.dispatch(Decrement(10)) // 200 - 90 = 110
        }
        assertEquals(110, sideEffectData.value)
    }

    @Test
    fun `should invoke middleware until remove`() {
        data class SideEffectData(var value: Int)

        val sideEffectData = SideEffectData(100)

        val middleware = AnyMiddleware { order: Order, _: CounterStore, state: CounterState, action: Any ->
            if (order == Order.BeforeReduce) {
                assertEquals(0, state.counter)
                assertTrue(action is Increment)
            } else {
                sideEffectData.value = sideEffectData.value + state.counter
            }
        }

        val localStore = Store(CounterState(), reducers, emptyMap())
        localStore.addMiddleware(Increment::class to middleware)

        runTest {
            localStore.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Increment(100))
        }
        assertEquals(200, sideEffectData.value)

        localStore.removeMiddleware(Increment::class to middleware)

        runTest {
            localStore.dispatch(Increment(100))
            localStore.dispatch(Decrement(100))
        }

        assertEquals(100, localStore.currentState.counter)
        assertEquals(200, sideEffectData.value) // Middleware for Increment is removed so there is no effect here
    }

    @Test
    fun `should invoke middleware in the correct order`() {
        val localStore = Store(CounterState(), reducers, mapOf(
            Increment::class to Middleware { order: Order, _: CounterStore, state: CounterState, _: Increment ->
                if (order == Order.BeforeReduce) {
                    assertEquals(0, state.counter)
                } else {
                    assertEquals(100, state.counter)
                }
            }
        ))

        runTest {
            localStore.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Increment(100))
        }
    }

    @Test
    fun `should invoke even we don't provide the customization on the identifier with the qualified name`() {
        val localStore = Store(CounterState(), mapOf(
            Set::class to Reducer { currentState: CounterState, action: Set ->
                currentState.copy(counter = action.value)
            }
        ))

        runTest {
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
        val localStore = Store(CounterState(), reducers, mapOf(
            Increment::class to Middleware { order: Order, store: CounterStore, state: CounterState, _: Increment ->
                if (order == Order.AfterReduce) {
                    if (state.counter == 100) {
                        // dispatch another action from middleware
                        runTest {
                            delay(1000)
                            store.dispatch(Increment(10))
                        }
                        store.tryDispatch(Decrement(200))
                    }
                }
            }
        ))

        runTest {
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
        runTest {
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
        runTest {
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

    class Multiply(val by: Int)
    class Divide(val by: Int)

    @Test
    fun `should be able to dynamically add new reducer into the store`() {
        runTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        3 -> assertEquals(900, state.counter)
                        4 -> assertEquals(180, state.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(10))
            store.dispatch(Decrement(1))

            val r1 = Multiply::class to Reducer { currentState: CounterState, action: Multiply ->
                currentState.copy(counter = currentState.counter * action.by)
            } as AnyReducer<CounterState>

            store.addReducer(r1)
            store.dispatch(Multiply(100))

            val r2 = Divide::class to Reducer { currentState: CounterState, action: Divide ->
                currentState.copy(counter = currentState.counter / action.by)
            } as AnyReducer<CounterState>

            store.addReducer(r2)
            store.dispatch(Divide(5))
        }
    }

    @Test
    fun `should be able to dynamically remove new reducer into the store`() {
        runTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> {} //do nothing
                        1 -> assertEquals(2, state.counter)
                        2 -> assertEquals(200, state.counter)
                        3 -> assertEquals(40, state.counter)
                        4 -> assertEquals(40, state.counter)
                        5 -> assertEquals(40, state.counter)
                        6 -> assertEquals(10, state.counter)
                        else -> error("Should not reach here")
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(2)) // 2

            val multiply = Multiply::class to Reducer { currentState: CounterState, action: Multiply ->
                currentState.copy(counter = currentState.counter * action.by)
            } as AnyReducer<CounterState>

            store.addReducer(multiply)
            store.dispatch(Multiply(100)) // 200

            val divide = Divide::class to Reducer { currentState: CounterState, action: Divide ->
                currentState.copy(counter = currentState.counter / action.by)
            } as AnyReducer<CounterState>

            store.addReducer(divide)
            store.dispatch(Divide(5)) // 40

            store.removeReducer(multiply)

            store.dispatch(Multiply(100)) // 40
            store.dispatch(Multiply(5)) // 40
            store.dispatch(Divide(4)) // Divide should still be usable
        }
    }
}
