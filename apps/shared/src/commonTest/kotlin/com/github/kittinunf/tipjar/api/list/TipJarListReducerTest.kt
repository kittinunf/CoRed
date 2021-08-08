package com.github.kittinunf.tipjar.api.list

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TipJarListReducerTest {

    @BeforeTest
    fun before() {
    }

    @Test
    fun `should update state to correct value when LoadTips is dispatch`() {
        val state = ListUiState()
        val (_, reducer) = LoadTipsReducer()
        val nextState = reducer(state, LoadTips(false))

        assertEquals(true, nextState.isLoading)
    }

    @Test
    fun `should update state to correct value when LoadTipsResultSuccess is dispatched`() {
        val state = ListUiState()
        val (_, reducer) = LoadTipsResultSuccessReducer()
        val nextState = reducer(state, LoadTipsResultSuccess(createRandomListUiItemState()))

        assertEquals(false, nextState.isLoading)
        assertEquals(3, nextState.list.size)
        assertEquals(1, nextState.list[0].id)
        assertEquals("2021 August 6", nextState.list[1].timestamp)
        assertEquals(2200f, nextState.list[2].amount)
        assertNull(nextState.errorMessage)
    }

    @Test
    fun `should update state to correct value when LoadTipsResultFailure is dispatched`() {
        val state = ListUiState()
        val (_, reducer) = LoadTipsResultFailureReducer()
        val nextState = reducer(state, LoadTipsResultFailure(IllegalStateException("Cannot load tips")))

        assertEquals(false, nextState.isLoading)
        assertEquals(0, nextState.list.size)
        assertEquals("Cannot load tips", nextState.errorMessage)
    }

    @Test
    fun `should update state to correct value when first LoadTipsResultSuccess is dispatched the LoadTipsResultFailure is`() {
        val state = ListUiState()
        val (_, reducer) = LoadTipsResultSuccessReducer()
        val s0 = reducer(state, LoadTipsResultSuccess(createRandomListUiItemState()))

        assertEquals(3, s0.list.size)

        val (_, nextReducer) = LoadTipsResultFailureReducer()
        val s1 = nextReducer(s0, LoadTipsResultFailure(IllegalStateException("Error")))

        assertEquals(3, s1.list.size)
        assertEquals("Error", s1.errorMessage)
    }

    @Test
    fun `should update state to correct value when first LoadTipsResultFailure is dispatched the LoadTipsResultSuccess is`() {
        val state = ListUiState()
        val (_, reducer) = LoadTipsResultFailureReducer()
        val s0 = reducer(state, LoadTipsResultFailure(IllegalStateException("Error")))

        assertEquals(0, s0.list.size)
        assertEquals("Error", s0.errorMessage)

        val (_, nextReducer) = LoadTipsResultSuccessReducer()
        val s1 = nextReducer(s0, LoadTipsResultSuccess(createRandomListUiItemState()))

        assertEquals(3, s1.list.size)
        assertEquals(null, s1.errorMessage)
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun createRandomListUiItemState(): List<ListUiItemState> {
    return buildList {
        add(ListUiItemState(id = 1, timestamp = "2021 August 5", amount = 1200f, tipAmount = 150f, image = null))
        add(ListUiItemState(id = 2, timestamp = "2021 August 6", amount = 200f, tipAmount = 10f, image = null))
        add(ListUiItemState(id = 3, timestamp = "2021 August 7", amount = 2200f, tipAmount = 200f, image = "file://foo.bar/3.png"))
    }
}
