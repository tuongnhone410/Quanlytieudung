package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale.forLanguageTag("vi-VN")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val decimalFormat = DecimalFormat("#,###", symbols)

    /**
     * Formats a Long amount to Vietnamese Dong string representation:
     * e.g. 50000 -> "50.000đ"
     *      0 -> "0đ"
     */
    fun formatVnd(amount: Long): String {
        return if (amount == 0L) {
            "0đ"
        } else {
            "${decimalFormat.format(amount)}đ"
        }
    }

    /**
     * Parses a raw input string containing digits to Long.
     */
    fun parseAmount(input: String): Long {
        val clean = input.filter { it.isDigit() }
        return clean.toLongOrNull() ?: 0L
    }

    /**
     * Formats numbers into formatted display while typing (e.g. 50000 -> 50.000)
     */
    fun formatInputNumber(amount: Long): String {
        return if (amount == 0L) "" else decimalFormat.format(amount)
    }
}
