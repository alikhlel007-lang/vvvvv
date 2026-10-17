package com.example.util

import kotlin.math.ceil
import kotlin.math.max

object OrderFeeCalculator {
    /**
     * Iraqi 1% fee calculation rule:
     * - Adds 1% to the order subtotal.
     * - Minimum fee is 250 IQD (because the minimum physical denomination in Iraq is 250 IQD).
     * - Rounded up to the nearest multiple of 250 IQD (e.g. 200 IQD -> 250 IQD, 600 IQD -> 750 IQD, etc.).
     *
     * If currency is USD ($), 1% is calculated with minimum $0.25 and rounded to 2 decimal places.
     */
    fun calculateServiceFee(subtotal: Double, currency: String = "د.ع"): Double {
        if (subtotal <= 0.0) return 0.0
        val isDollar = currency == "$" || currency.equals("USD", ignoreCase = true)
        return if (isDollar) {
            val raw = subtotal * 0.01
            max(raw, 0.25)
        } else {
            val rawOnePercent = subtotal * 0.01
            val minFee = 250.0
            val feeToRound = max(rawOnePercent, minFee)
            ceil(feeToRound / 250.0) * 250.0
        }
    }

    /**
     * Total amount including the 1% rounded fee.
     */
    fun calculateTotal(subtotal: Double, currency: String = "د.ع"): Double {
        return subtotal + calculateServiceFee(subtotal, currency)
    }

    /**
     * Formats prices with clean typography and proper currency symbol.
     */
    fun formatPrice(amount: Double, currency: String = "د.ع"): String {
        val isDollar = currency == "$" || currency.equals("USD", ignoreCase = true)
        return if (isDollar) {
            String.format(java.util.Locale.US, "%.2f $", amount)
        } else {
            String.format(java.util.Locale.US, "%,d د.ع", amount.toLong())
        }
    }
}
