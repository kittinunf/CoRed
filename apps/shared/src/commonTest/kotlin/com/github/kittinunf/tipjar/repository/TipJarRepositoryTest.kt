package com.github.kittinunf.tipjar.repository

import com.github.kittinunf.tipjar.api.di.platformTestModule
import com.github.kittinunf.tipjar.api.di.startKoin
import com.github.kittinunf.tipjar.util.BaseInstrumentTest
import com.github.kittinunf.tipjar.util.runBlockingTest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TipJarRepositoryTest : BaseInstrumentTest(), KoinComponent {

    private val repository by inject<TipJarRepository>()

    override fun setUp() {
        startKoin(additionalModules = listOf(platformTestModule()))

        runBlockingTest {
            repository.deleteAllTips()
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
    }

    private data class Tip(val id: Int, val amount: Int, val tipAmount: Int)

    @Test
    fun `should add tip into the repository correctly`() {
        val tips = runBlockingTest {
            repository.createTip(amount = 1_000, peopleCount = 2, tipAmount = 15, timestamp = 1)

            repository.getTips { id, amount, _, tipAmount, _, _ ->
                Tip(id.toInt(), amount.toInt(), tipAmount.toInt())
            }.executeAsList()
        }

        assertTrue(tips.isNotEmpty())
        assertEquals(1_000, tips[0].amount)
        assertEquals(15, tips[0].tipAmount)
    }

    @Test
    fun `should get tips from the repository correctly`() {
        val tips = runBlockingTest {
            repository.createTip(amount = 1_000, peopleCount = 2, tipAmount = 2, timestamp = 1)
            repository.createTip(amount = 1_100, peopleCount = 3, tipAmount = 3, timestamp = 1)
            repository.createTip(amount = 1_200, peopleCount = 4, tipAmount = 2, timestamp = 1)
            repository.createTip(amount = 1_300, peopleCount = 5, tipAmount = 4, timestamp = 1)

            repository.getTips { id, amount, _, tipAmount, _, _ ->
                Tip(id.toInt(), amount.toInt(), tipAmount.toInt())
            }.executeAsList()
        }

        assertTrue(tips.isNotEmpty())
        assertTrue(tips.count() == 4)
        assertEquals(1_000, tips[0].amount)
        assertEquals(3, tips[1].tipAmount)
        assertEquals(1_200, tips[2].amount)
    }

    @Test
    fun `should get tips from the repositroy in the correct order`() {
        val tips = runBlockingTest {
            repository.createTip(amount = 1_000, peopleCount = 2, tipAmount = 2, timestamp = 1)
            repository.createTip(amount = 1_100, peopleCount = 3, tipAmount = 3, timestamp = 2)
            repository.createTip(amount = 1_200, peopleCount = 4, tipAmount = 2, timestamp = 3)
            repository.createTip(amount = 1_300, peopleCount = 5, tipAmount = 4, timestamp = 4)

            repository.getTipsDesc { id, amount, _, tipAmount, _, _ ->
                Tip(id.toInt(), amount.toInt(), tipAmount.toInt())
            }.executeAsList()
        }

        assertTrue(tips.isNotEmpty())
        assertEquals(4, tips.count())
        assertEquals(1_300, tips[0].amount)
        assertEquals(2, tips[1].tipAmount)
        assertTrue(tips[0].id > tips[1].id)
    }

    @Test
    fun `should delete tip by id from the repository correctly`() {
        var tips = runBlockingTest {
            repository.createTip(amount = 1_000, peopleCount = 2, tipAmount = 200, timestamp = 1)
            repository.createTip(amount = 1_100, peopleCount = 3, tipAmount = 300, timestamp = 1)

            repository.getTips { id, amount, _, tipAmount, _, _ ->
                Tip(id.toInt(), amount.toInt(), tipAmount.toInt())
            }.executeAsList()
        }

        assertTrue(tips.count() == 2)

        tips = runBlockingTest {
            repository.deleteTip(tips[0].id)

            repository.getTips { id, amount, _, tipAmount, _, _ ->
                Tip(id.toInt(), amount.toInt(), tipAmount.toInt())
            }.executeAsList()
        }

        assertTrue(tips.count() == 1)
    }

    @Test
    fun `should delete all tip from the repository correctly`() {
        var tips = runBlockingTest {
            repository.createTip(amount = 1_000, peopleCount = 2, tipAmount = 200, timestamp = 1)
            repository.createTip(amount = 1_100, peopleCount = 3, tipAmount = 300, timestamp = 1)
            repository.createTip(amount = 2_100, peopleCount = 3, tipAmount = 300, timestamp = 1)
            repository.createTip(amount = 2_200, peopleCount = 3, tipAmount = 300, timestamp = 1)

            repository.getTips { id, amount, _, tipAmount, _, _ ->
                Tip(id.toInt(), amount.toInt(), tipAmount.toInt())
            }.executeAsList()
        }

        assertTrue(tips.count() == 4)

        tips = runBlockingTest {
            repository.deleteAllTips()

            repository.getTips { id, amount, _, tipAmount, _, _ ->
                Tip(id.toInt(), amount.toInt(), tipAmount.toInt())
            }.executeAsList()
        }

        assertTrue(tips.count() == 0)
    }

}
