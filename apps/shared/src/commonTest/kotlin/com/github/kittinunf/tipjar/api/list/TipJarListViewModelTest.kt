package com.github.kittinunf.tipjar.api.list

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

class TipJarListViewModelTest : BaseInstrumentTest(), KoinComponent {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private val repository by inject<TipJarRepository>()
    private val vm by inject<TipJarListViewModel>()

    override fun setUp() {
        startKoin(additionalModules = listOf(platformTestModule()))

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
    fun `should load all of the tips correctly`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    when (index) {
                        0 -> {
                            // starting ignore
                        }
                        1 -> {
                            assertTrue(state.isLoading)
                        }
                        2 -> {
                            assertFalse(state.isLoading)
                            assertEquals(3, state.list.size)
                            assertEquals(338.43f, state.list[0].amount)
                            assertEquals(13.84f, state.list[1].tipAmount)
                            assertEquals("file://foo/bar/1.png", state.list[2].image)
                        }
                        else -> {
                        }
                    }
                }
                .launchIn(testScope)

            repository.createTip(33_843, 2, 3893, null, 1)
            repository.createTip(12_843, 5, 1384, null, 2)
            repository.createTip(42_843, 10, 4798, "file://foo/bar/1.png", 3)

            vm.loadTips(false)
        }
    }

    @Test
    fun `should able to update list of tips correctly when the service is updated`() {
        runBlockingTest {
            vm.states
                .withIndex()
                .onEach { (index, state) ->
                    println("$index, $state")
                    when (index) {
                        0 -> {
                            // starting ignore
                        }
                        1 -> {
                            assertTrue(state.isLoading)
                        }
                        2 -> {
                            assertFalse(state.isLoading)
                            assertEquals(1, state.list.size)
                            assertEquals(338.43f, state.list[0].amount)
                            assertEquals(38.93f, state.list[0].tipAmount)

                            // add new tip
                            repository.createTip(1_200, 1, 220, null, 2)
                        }
                        3 -> {
                            assertEquals(2, state.list.size)
                            // the later one comes first
                            assertEquals(12.00f, state.list[0].amount)
                            assertEquals(2.20f, state.list[0].tipAmount)
                            assertEquals(338.43f, state.list[1].amount)
                            assertEquals(38.93f, state.list[1].tipAmount)
                        }
                        else -> {
                        }
                    }
                }
                .launchIn(testScope)

            repository.createTip(33_843, 2, 3893, null, 1)
        }

        vm.loadTips(true)
    }
}
