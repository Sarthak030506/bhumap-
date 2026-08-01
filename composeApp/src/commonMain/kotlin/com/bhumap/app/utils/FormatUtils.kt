package com.bhumap.app.utils

/**
 * Format a Double as Indian Rupee format.
 * e.g. 1234567.89 → "₹12,34,567.89"
 */
fun Double.formatINR(): String {
    val isNegative = this < 0
    val abs = if (isNegative) -this else this
    val parts = "%.2f".format(abs).split(".")
    val intPart = parts[0]
    val decPart = parts[1]

    val result = when {
        intPart.length <= 3 -> intPart
        else -> {
            val last3 = intPart.takeLast(3)
            val rest  = intPart.dropLast(3)
