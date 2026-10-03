package com.metron.app.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Robust financial calculation and formatting utility.
 * Prevents IEEE-754 double precision drift and validates monetary input bounds.
 */
object MoneyUtils {

    const val MAX_ALLOWED_AMOUNT = 100_000_000_000.0 // ₹10,000 Crore limit
    const val MIN_ALLOWED_AMOUNT = 0.0

    /**
     * Rounds a double amount to exactly 2 decimal places using HALF_EVEN (Banker's rounding).
     * Replaces NaN or Infinite with 0.0.
     */
    fun round(amount: Double): Double {
        if (amount.isNaN() || amount.isInfinite()) return 0.0
        val clamped = amount.coerceIn(-MAX_ALLOWED_AMOUNT, MAX_ALLOWED_AMOUNT)
        return BigDecimal.valueOf(clamped)
            .setScale(2, RoundingMode.HALF_EVEN)
            .toDouble()
    }

    /**
     * Checks if a monetary amount is valid, non-negative, and within realistic limits.
     */
    fun isValidAmount(amount: Double): Boolean {
        return !amount.isNaN() && !amount.isInfinite() && amount >= MIN_ALLOWED_AMOUNT && amount <= MAX_ALLOWED_AMOUNT
    }

    /**
     * Parses a string representation of an amount into a rounded Double, or null if invalid.
     */
    fun parseAmount(text: String): Double? {
        val clean = text.trim().replace(",", "")
        val raw = clean.toDoubleOrNull() ?: return null
        if (!isValidAmount(raw)) return null
        return round(raw)
    }

    /**
     * Formats an amount with currency symbol and localized digit grouping.
     * Whole numbers format without decimals (e.g. ₹1,200), decimals format to 2 places (e.g. ₹1,200.50).
     */
    fun format(currencySymbol: String, amount: Double, locale: Locale = Locale.getDefault()): String {
        val rounded = round(amount)
        return if (rounded % 1.0 == 0.0) {
            "$currencySymbol${String.format(locale, "%,.0f", rounded)}"
        } else {
            "$currencySymbol${String.format(locale, "%,.2f", rounded)}"
        }
    }

    /**
     * Safely sums a list of amounts preventing floating-point accumulation errors.
     */
    fun sum(amounts: Iterable<Double>): Double {
        var total = BigDecimal.ZERO
        for (a in amounts) {
            if (!a.isNaN() && !a.isInfinite()) {
                total = total.add(BigDecimal.valueOf(a))
            }
        }
        return total.setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }
}
