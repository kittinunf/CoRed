package com.github.kittinunf.tipjar.util

expect abstract class BaseInstrumentTest() {

    abstract fun setUp()
}

expect fun initialize(): Any
