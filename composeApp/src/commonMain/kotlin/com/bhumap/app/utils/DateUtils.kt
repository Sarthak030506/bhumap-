package com.bhumap.app.utils

import kotlinx.datetime.*
import kotlinx.datetime.format.char

/**
 * Basic Date formatting utilities using kotlinx-datetime.
 */
fun String.toFormattedDate(): String {
    return try {
        val instant = Instant.parse(this)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        "${localDate.dayOfMonth} ${localDate.month.name.take(3)} ${localDate.year}"
    } catch (e: Exception) {
        this
    }
}

fun getCurrentDateIso(): String {
    return Clock.System.now().toString()
}
