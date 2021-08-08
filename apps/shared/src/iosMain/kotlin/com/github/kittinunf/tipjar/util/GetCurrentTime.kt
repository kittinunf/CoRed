package com.github.kittinunf.tipjar.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getCurrentTimestampInSeconds(): Long = NSDate().timeIntervalSince1970.toLong()
