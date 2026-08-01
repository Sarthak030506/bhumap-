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
            val groups = rest.reversed().chunked(2).joinToString(",") { it.reversed() }.reversed()
            "$groups,$last3"
        }
    }
    return "${if (isNegative) "-" else ""}₹$result.$decPart"
}

/** Compact version: "₹12.3L", "₹4.5Cr" */
fun Double.formatINRCompact(): String = when {
    this >= 1_00_00_000 -> "₹${"%.1f".format(this / 1_00_00_000)}Cr"
    this >= 1_00_000    -> "₹${"%.1f".format(this / 1_00_000)}L"
    this >= 1_000       -> "₹${"%.1f".format(this / 1_000)}K"
    else                -> formatINR()
}

/** Strip non-digits, ensure +91 prefix */
fun normalisePhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return if (digits.startsWith("91") && digits.length == 12) "+$digits"
    else if (digits.length == 10) "+91$digits"
    else "+$digits"
}
