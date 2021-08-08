package com.github.kittinunf.tipjar.api.list

import com.github.kittinunf.tipjar.util.BaseInstrumentTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TipJarListMapperTest : BaseInstrumentTest() {

    override fun setUp() {
    }

    @Test
    fun `should map the object from db to the ListUiItemState correctly`() {
        val listItem = ListUiItemStateMapper(id = 111, amount = 2_200, peopleCount = 3, tipAmount = 528, imageLocation = null, time = 1628095577, "UTC")
        assertEquals(22f, listItem.amount)
        assertEquals(5.28f, listItem.tipAmount)
        assertEquals("2021 Aug 04", listItem.timestamp)
        assertNull(listItem.image)

        val anotherListItem = ListUiItemStateMapper(id = 222, amount = 3843, peopleCount = 1, tipAmount = 389, imageLocation = "file://foo.bar", time = 1628247216, "UTC")

        assertEquals(38.43f, anotherListItem.amount)
        assertEquals(3.89f, anotherListItem.tipAmount)
        assertEquals("2021 Aug 06", anotherListItem.timestamp)
        assertEquals("file://foo.bar", anotherListItem.image)
    }
}
