package com.github.kittinunf.tipjar.util

import java.util.concurrent.TimeUnit

actual fun getCurrentTimestampInSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
