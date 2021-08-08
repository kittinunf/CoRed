package com.github.kittinunf.tipjar.api.input

import com.github.kittinunf.tipjar.api.di.platformTestModule
import com.github.kittinunf.tipjar.api.di.startKoin
import com.github.kittinunf.tipjar.repository.TipJarRepository
import com.github.kittinunf.tipjar.util.BaseInstrumentTest
import com.github.kittinunf.tipjar.util.runBlockingTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TipJarInputViewModelTest : BaseInstrumentTest(), KoinComponent {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private val repository by inject<TipJarRepository>()
    private val vm by inject<TipJarInputViewModel>()

    override fun setUp() {
        startKoin({ allowOverride(true) }, additionalModules = listOf(platformTestModule()))

        // clear database
        runBlockingTest {
            repository.deleteAllTips()
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
        testScope.cancel()
    }

    @Test
    fun `should load initial tip for starting the screen correctly`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> {
                            // starting ignore
                        }
                        1 -> {
                            assertEquals(0f, state.amount)
                            assertEquals(1, state.peopleCount)
                            assertEquals(0, state.tipPercentage)
                            assertEquals(0f, state.totalTipAmount)
                            assertEquals(0f, state.tipPerPerson)
                            assertFalse(state.isPhotoEnabled)
                        }
                        else -> error("")
                    }
                }
                .launchIn(testScope)

            // load
            vm.setInitialTip()
        }
    }

    @Test
    fun `should update amount and tip amount on the screen correctly`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        1 -> {
                            assertEquals(0f, state.amount)
                            assertEquals(1, state.peopleCount)
                            assertEquals(0, state.tipPercentage)
                            assertEquals(0f, state.totalTipAmount)
                            assertEquals(0f, state.tipPerPerson)
                            assertFalse(state.isPhotoEnabled)
                        }
                        3 -> {
                            assertEquals(10f, state.amount)
                            assertEquals(1f, state.totalTipAmount)
                            assertEquals(1f, state.tipPerPerson)
                        }
                        4 -> {
                            assertEquals(150f, state.amount)
                            assertEquals(15f, state.totalTipAmount)
                            assertEquals(15f, state.tipPerPerson)
                        }
                        5 -> {
                            assertEquals(248f, state.amount)
                            assertEquals(24.8f, state.totalTipAmount)
                            assertEquals(24.8f, state.tipPerPerson)
                        }
                    }
                }
                .launchIn(testScope)

            vm.setInitialTip()

            vm.updateTipPercentage(10)
            vm.updateAmount(10f)
            vm.updateAmount(150f)
            vm.updateAmount(248f)
        }
    }

    @Test
    fun `should update people and tip amount on the screen correctly`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        3 -> {
                            assertEquals(248f, state.amount)
                            assertEquals(24.8f, state.totalTipAmount)
                            assertEquals(24.8f, state.tipPerPerson)
                        }
                        4 -> {
                            assertEquals(248f, state.amount)
                            assertEquals(24.8f, state.totalTipAmount)
                            assertEquals(3, state.peopleCount)
                            assertEquals(8.266666f, state.tipPerPerson)
                        }
                        5 -> {
                            assertEquals(36f, state.totalTipAmount)
                            assertEquals(12f, state.tipPerPerson)
                        }
                        6 -> {
                            assertEquals(4, state.peopleCount)
                            assertEquals(9f, state.tipPerPerson)
                        }
                    }
                }
                .launchIn(testScope)

            vm.setInitialTip()
            vm.updateTipPercentage(10)

            vm.updateAmount(248f)
            vm.updatePeopleCount(3)
            vm.updateAmount(360f)
            vm.updatePeopleCount(4)
        }
    }

    @Test
    fun `should update tip percentage and tip amount on the screen correctly`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        3 -> {
                            assertEquals(735f, state.amount)
                            assertEquals(73.5f, state.totalTipAmount)
                            assertEquals(5, state.peopleCount)
                            assertEquals(14.7f, state.tipPerPerson)
                        }
                        4 -> {
                            assertEquals(110.25f, state.totalTipAmount)
                            assertEquals(22.05f, state.tipPerPerson)
                        }
                        5 -> {
                            assertEquals(147f, state.totalTipAmount)
                            assertEquals(29.4f, state.tipPerPerson)
                        }
                    }
                }
                .launchIn(testScope)

            //0
            vm.updateTipPercentage(10) //1

            vm.updateAmount(735f) //2
            vm.updatePeopleCount(5) //3
            vm.updateTipPercentage(15) //4
            vm.updateTipPercentage(20)
        }
    }

    @Test
    fun `should update the take photo status on the screen correctly`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> {
                            // starting ignore
                        }
                        1 -> {
                            assertEquals(0f, state.amount)
                            assertEquals(1, state.peopleCount)
                            assertEquals(0, state.tipPercentage)
                            assertEquals(0f, state.totalTipAmount)
                            assertEquals(0f, state.tipPerPerson)
                            assertFalse(state.isPhotoEnabled)
                        }
                        2 -> {
                            assertTrue(state.isPhotoEnabled)
                        }
                        3 -> {
                            assertFalse(state.isPhotoEnabled)
                        }
                        else -> error("")
                    }
                }
                .launchIn(testScope)

            vm.setInitialTip()

            vm.updatePhotoEnabled(true)
            vm.updatePhotoEnabled(false)
        }
    }

    @Test
    fun `should save tip into the database correctly with camera and without`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> {
                            // starting ignore
                        }
                        1 -> {
                            assertEquals(0f, state.amount)
                            assertEquals(1, state.peopleCount)
                            assertEquals(0, state.tipPercentage)
                            assertEquals(0f, state.totalTipAmount)
                            assertEquals(0f, state.tipPerPerson)
                            assertFalse(state.isPhotoEnabled)
                        }
                    }
                }
                .launchIn(testScope)
        }

        vm.setInitialTip()
        vm.updateAmount(1003f)
        vm.updatePeopleCount(5)
        vm.saveTip()

        val query = runBlockingTest { repository.getTips { _, amount, peopleCount, _, image, _ -> Triple(amount, peopleCount, image) } }
        val tips = query.executeAsList()
        assertEquals(1, tips.size)
        assertEquals(1003f, tips[0].first / 100f)
        assertEquals(5, tips[0].second)

        vm.updatePhotoEnabled(true)
        vm.saveTip("file://foo/bar/1.png")

        val query2 = runBlockingTest { repository.getTips { _, amount, peopleCount, _, image, _ -> Triple(amount, peopleCount, image) } }
        val tips2 = query2.executeAsList()
        assertEquals(2, tips2.size)
        assertEquals("file://foo/bar/1.png", tips2[1].third)
    }
}
