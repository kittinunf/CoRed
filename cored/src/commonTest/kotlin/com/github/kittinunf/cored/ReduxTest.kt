package com.github.kittinunf.cored

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

// State definition
data class CounterState(val counter: Int = 0)

// Action definition
class Increment(val by: Int)
class Decrement(val by: Int)
class Set(val value: Int)

typealias CounterStore = StoreType<CounterState>

class ReduxTest {

    private val counterState = CounterState()
    private val counterReducer = AnyReducer { currentState: CounterState, action ->
        with(currentState) {
            when (action) {
                is Increment -> copy(counter = counter + action.by)
                is Decrement -> copy(counter = counter - action.by)
                is Set -> copy(counter = action.value)
                else -> currentState
            }
        }
    }

    private val testScope = CoroutineScope(Dispatchers.Unconfined)
    private val store = Store(testScope, counterState, counterReducer)

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
    fun `should emit value if the state not changed`() {
        runTest {
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
        runTest {
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

        val middleware = AnyMiddleware { order: Order, store: CounterStore, state: CounterState, action: Any ->
            if (order == Order.BeforeReduce) {
                assertEquals(0, state.counter)
                assertTrue(action is Increment)
            } else {
                sideEffectData.value = sideEffectData.value + state.counter
            }
        }

        store.addMiddleware(middleware)

        runTest {
            store.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(100))
        }

        assertEquals(200, sideEffectData.value)
    }

    @Test
    fun `should invoke middleware until remove`() {
        data class SideEffectData(var value: Int)

        val sideEffectData = SideEffectData(100)

        val middleware = AnyMiddleware { order: Order, store: CounterStore, state: CounterState, action: Any ->
            if (order == Order.BeforeReduce) {
                assertEquals(0, state.counter)
                assertTrue(action is Increment)
            } else {
                sideEffectData.value = sideEffectData.value + state.counter
            }
        }

        store.addMiddleware(middleware)

        runTest {
            store.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(100))
        }

        assertEquals(200, sideEffectData.value)

        store.removeMiddleware(middleware)

        runTest {
            store.dispatch(Increment(100))
            store.dispatch(Decrement(100))
        }

        assertEquals(200, sideEffectData.value)
    }

    @Test
    fun `should invoke middleware in the correct order`() {
        val middleware = AnyMiddleware { order: Order, store: CounterStore, state: CounterState, action: Any ->
            if (order == Order.BeforeReduce) {
                assertEquals(0, state.counter)
                assertTrue(action is Increment)
            } else {
                assertEquals(100, state.counter)
                assertTrue(action is Increment)
            }
        }

        store.addMiddleware(middleware)

        runTest {
            store.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(100))
        }
    }

    @Test
    fun `should be able to dispatch action from the middleware`() {
        val middleware = AnyMiddleware { order: Order, store: StoreType<CounterState>, state: CounterState, action: Any ->
            if (order == Order.BeforeReduce) {
                assertTrue(action is Increment)
            } else {
                assertTrue(action is Increment)
                if (state.counter == 100) {
                    // dispatch another action from middleware
                    runTest {
                        store.dispatch(Increment(10))
                    }
                    store.tryDispatch(Increment(200))
                }
            }
        }

        store.addMiddleware(middleware)

        runTest {
            store.states
                .withIndex()
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(100))
        }

        assertEquals(310, store.currentState.counter)
    }

    class Multiply(val by: Int)
    class Divide(val by: Int)

    @Test
    fun `should ignore action that is not unknown with the current known action reducer`() {
        runTest {
            store.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        1 -> assertEquals(100, state.counter)
                        2 -> assertEquals(99, state.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            store.dispatch(Increment(100))
            store.dispatch(Multiply(10))
            store.dispatch(Divide(2))
            store.dispatch(Decrement(1))
        }
    }

    @Test
    fun `should able to use combine reducer as usual reducer`() {
        val localReducer = AnyReducer { currentState: CounterState, action ->
            when (action) {
                is Multiply -> currentState.copy(counter = currentState.counter * action.by)
                is Divide -> currentState.copy(counter = currentState.counter / action.by)
                else -> currentState
            }
        }

        val localStore = Store(testScope, counterState, combineReducers(localReducer, counterReducer))

        runTest {
            localStore.states
                .withIndex()
                .onEach { (index, value) ->
                    when (index) {
                        1 -> assertEquals(10, value.counter)
                        2 -> assertEquals(200, value.counter)
                        3 -> assertEquals(195, value.counter)
                        4 -> assertEquals(39, value.counter)
                    }
                }
                .printDebug()
                .launchIn(testScope)

            localStore.dispatch(Increment(10))
            localStore.dispatch(Multiply(20)) // 10 * 20
            localStore.dispatch(Decrement(5)) // 200 - 5 = 195
            localStore.dispatch(Divide(5)) // 195/5 = 39
        }
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
}
