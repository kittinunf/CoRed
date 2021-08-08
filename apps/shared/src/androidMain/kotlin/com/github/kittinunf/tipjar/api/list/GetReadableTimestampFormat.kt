package com.github.kittinunf.tipjar.api.list

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import java.util.*

actual fun getReadableTimestampFormat(timestamp: Long, pattern: String, tz: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone(tz) }.format(Date(timestamp * 1000))
}

actual fun getCurrentTimezone(): String = TimeZone.getDefault().id
