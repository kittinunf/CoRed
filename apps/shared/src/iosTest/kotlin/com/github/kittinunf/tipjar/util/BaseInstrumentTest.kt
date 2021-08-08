package com.github.kittinunf.tipjar.util

import kotlin.test.BeforeTest

actual abstract class BaseInstrumentTest {

    @BeforeTest
    actual abstract fun setUp()
}

actual fun initialize(): Any = Unit
