package com.github.kittinunf.tipjar.api.list

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.abbreviation
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.defaultTimeZone
import platform.Foundation.timeZoneWithAbbreviation
import platform.Foundation.timeZoneWithName

actual fun getReadableTimestampFormat(timestamp: Long, pattern: String, tz: String): String {
    return NSDateFormatter().apply {
        dateFormat = pattern
        timeZone = NSTimeZone.timeZoneWithAbbreviation(tz)!!
    }.stringFromDate(NSDate.dateWithTimeIntervalSince1970(timestamp.toDouble()))
}

actual fun getCurrentTimezone(): String = NSTimeZone.defaultTimeZone.abbreviation!!
