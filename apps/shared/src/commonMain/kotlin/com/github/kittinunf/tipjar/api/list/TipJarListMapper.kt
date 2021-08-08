package com.github.kittinunf.tipjar.api.list

typealias Mapper = (id: Long, amount: Long, peopleCount: Long, tipAmount: Long, imageLocation: String?, time: Long) -> ListUiItemState

@Suppress("FunctionName")
internal fun ListUiItemStateMapper(id: Long, amount: Long, peopleCount: Long, tipAmount: Long, imageLocation: String?, time: Long, tz: String = getCurrentTimezone()): ListUiItemState {
    return ListUiItemState(id = id.toInt(), timestamp = getReadableTimestampFormat(time, "yyyy MMM dd", tz), amount = (amount / 100f), tipAmount = (tipAmount / 100f), image = imageLocation)
}

expect fun getReadableTimestampFormat(timestamp: Long, pattern: String, tz: String): String
expect fun getCurrentTimezone(): String
