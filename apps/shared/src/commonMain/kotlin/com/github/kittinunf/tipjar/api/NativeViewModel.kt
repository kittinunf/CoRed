package com.github.kittinunf.tipjar.api

import kotlinx.coroutines.CoroutineScope

expect open class NativeViewModel() {

    val scope: CoroutineScope

    fun cancel()
}
