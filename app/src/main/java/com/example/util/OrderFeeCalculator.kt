package com.example.util

import kotlin.math.ceil
import kotlin.math.max

object OrderFeeCalculator {
    /**
     * 1% service fee without rounding during initial ordering stages (جديد / تحضير).
     */
    fun calculateServiceFee(subtotal: Double, currency: String = "د.ع"): Double {
        if (subtotal <= 0.0) return 0.0
        return subtotal * 0.01
    }

    /**
     * Raw total before settlement rounding.
     */
    fun calculateTotal(subtotal: Double, currency: String = "د.ع"): Double {
        return subtotal + calculateServiceFee(subtotal, currency)
    }

    /**
     * Rounded total applied at payment/settlement stage (تسديد حساب الطاولة).
     * For IQD: Rounded up to the nearest 250 IQD (standard physical currency increment in Iraq).
     * For USD: Rounded to 2 decimal places.
     */
    fun calculateSettlementTotal(subtotal: Double, currency: String = "د.ع"): Double {
        if (subtotal <= 0.0) return 0.0
        val rawTotal = subtotal + (subtotal * 0.01)
        val isDollar = currency == "$" || currency.equals("USD", ignoreCase = true)
        return if (isDollar) {
            kotlin.math.round(rawTotal * 100.0) / 100.0
        } else {
            ceil(rawTotal / 250.0) * 250.0
        }
    }

    /**
     * Effective service fee at settlement after rounding.
     */
    fun calculateSettlementFee(subtotal: Double, currency: String = "د.ع"): Double {
        val total = calculateSettlementTotal(subtotal, currency)
        return max(0.0, total - subtotal)
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
