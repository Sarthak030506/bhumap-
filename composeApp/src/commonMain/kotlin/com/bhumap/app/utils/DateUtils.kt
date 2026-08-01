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
