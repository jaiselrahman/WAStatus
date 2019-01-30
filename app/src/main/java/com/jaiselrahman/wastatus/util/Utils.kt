package com.jaiselrahman.wastatus.util

import java.math.RoundingMode
import java.text.DecimalFormat

fun Long?.compactString(): String {
    if (this == null) return "0"
    if (this < 1000) return toString()
    val exp = (Math.log10(toDouble()) / Math.log10(1000.0)).toInt()
    val value = this / Math.pow(1000.0, exp.toDouble())
    return decimalFormat.format(value) + "kMGTPE"[exp - 1]
}

private val decimalFormat by lazy {
    val decimalFormat = DecimalFormat("#,##0.#")
    decimalFormat.roundingMode = RoundingMode.FLOOR
    decimalFormat
}
