package com.github.kittinunf.tipjar.api.input

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TipJarInputReducerTest {

    @BeforeTest
    fun before() {
    }

    @Test
    fun `should update state to correct default tip state when SetTip is dispatched`() {
        val state = InputUiState()
        val (_, reducer) = SetTipReducer()
        val nextState = reducer(state, SetTip())

        assertEquals(0.0f, nextState.amount)
        assertEquals(1, nextState.peopleCount)
        assertEquals(0, nextState.tipPercentage)
        assertEquals(false, nextState.isPhotoEnabled)
    }

    @Test
    fun `should update state to correct custom tip state when SetTip is dispatched`() {
        val state = InputUiState()
        val (_, reducer) = SetTipReducer()
        val nextState = reducer(state, SetTip(InputUiState(1_200f, 5, 20, 100f, 20f, true)))

        assertEquals(1_200f, nextState.amount)
        assertEquals(5, nextState.peopleCount)
        assertEquals(20, nextState.tipPercentage)
        assertEquals(true, nextState.isPhotoEnabled)
    }

    @Test
    fun `should update state to correct value when UpdateAmount is dispatched`() {
        val state = InputUiState()
        val (_, setTipReducer) = SetTipReducer()
        val s1 = setTipReducer(state, SetTip(InputUiState(1_200f, 5, 20, 100f, 20f, true)))

        val (_, reducer) = UpdateAmountReducer()
        val s2 = reducer(s1, UpdateAmount(2000f))

        assertEquals(2000f, s2.amount)
        assertEquals(400f, s2.totalTipAmount) //2_000, tip 20 % = 400
        assertEquals(80f, s2.tipPerPerson)

        val s3 = reducer(s2, UpdateAmount(2500f))

        assertEquals(500f, s3.totalTipAmount) //2_500, tip 20 %
        assertEquals(100f, s3.tipPerPerson)

        val s4 = reducer(s3, UpdateAmount(3240f))
        assertEquals(648f, s4.totalTipAmount) //3240, tip 20 %
        assertEquals(129.6f, s4.tipPerPerson)
    }

    @Test
    fun `should update state to correct value when UpdatePeopleCount is dispatched`() {
        val state = InputUiState()
        val (_, setTipReducer) = SetTipReducer()
        val s1 = setTipReducer(state, SetTip(InputUiState(1_200f, 5, 20, 240f, 20f, true)))

        val (_, reducer) = UpdatePeopleCountReducer()
        val s2 = reducer(s1, UpdatePeopleCount(4))

        assertEquals(1_200f, s2.amount)
        assertEquals(4, s2.peopleCount)
        assertEquals(60f, s2.tipPerPerson)

        val s3 = reducer(s2, UpdatePeopleCount(3))
        assertEquals(3, s3.peopleCount)
        assertEquals(80f, s3.tipPerPerson)

        val s4 = reducer(s3, UpdatePeopleCount(2))
        assertEquals(2, s4.peopleCount)
        assertEquals(120f, s4.tipPerPerson)
    }

    @Test
    fun `should update state to correct value when UpdateTipPercentage is dispatched`() {
        val state = InputUiState()
        val (_, setTipReducer) = SetTipReducer()
        val s1 = setTipReducer(state, SetTip(InputUiState(1_200f, 5, 20, 240f, 20f, true)))

        val (_, reducer) = UpdateTipPercentageReducer()
        val s2 = reducer(s1, UpdateTipPercentage(10))

        assertEquals(1_200f, s2.amount)
        assertEquals(120f, s2.totalTipAmount) //1200, tip 10 % = 120
        assertEquals(24f, s2.tipPerPerson)

        val s3 = reducer(s2, UpdateTipPercentage(20))

        assertEquals(240f, s3.totalTipAmount) //1200, tip 20 % = 240
        assertEquals(48f, s3.tipPerPerson)

        val s4 = reducer(s3, UpdateTipPercentage(50))
        assertEquals(600f, s4.totalTipAmount) //1200, tip 50
        assertEquals(120f, s4.tipPerPerson)
    }

    @Test
    fun `should update state to correct value when UpdatePhotoEnable`() {
        val state = InputUiState()
        val (_, reducer) = UpdatePhotoEnableReducer()

        val newState = reducer(state, UpdatePhotoEnabled(true))
        assertEquals(true, newState.isPhotoEnabled)

        val s2 = reducer(newState, UpdatePhotoEnabled(false))
        assertEquals(false, s2.isPhotoEnabled)
    }

    @Test
    fun `should not update state when SaveTip is dispatched`() {
        val state = InputUiState()
        val (_, setTipReducer) = SetTipReducer()
        val s1 = setTipReducer(state, SetTip(InputUiState(1_200f, 5, 20, 100f, 20f, true)))

        val (_, reducer) = SaveTipReducer()
        val s2 = reducer(s1, SaveTip())

        assertEquals(1_200f, s2.amount)
        assertEquals(5, s2.peopleCount)
        assertEquals(20, s2.tipPercentage)

        val s3 = reducer(s2, SaveTip("file://foo/bar/1.png"))

        assertEquals(1_200f, s3.amount)
        assertEquals(5, s3.peopleCount)
        assertEquals(20, s3.tipPercentage)
    }
}
