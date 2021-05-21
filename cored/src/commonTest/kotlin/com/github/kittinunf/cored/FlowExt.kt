package com.github.kittinunf.cored

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

internal fun <T> Flow<T>.printDebug() = onEach { println(it) }
