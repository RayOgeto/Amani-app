package com.refresh.pos.domain.engine

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    private val integerFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    fun formatKsh(amount: Double, withDecimals: Boolean = true): String {
        val formatted = if (withDecimals || amount % 1.0 != 0.0) {
            numberFormat.format(amount)
        } else {
            integerFormat.format(amount)
        }
        return "KSh $formatted"
    }

    fun format(amount: Double): String = formatKsh(amount)

    fun formatQuantity(quantity: Double, uom: String = "PCS"): String {
        val qtyStr = if (quantity % 1.0 == 0.0) {
            quantity.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", quantity).trimEnd('0').trimEnd('.')
        }
        return "$qtyStr $uom"
    }
}
