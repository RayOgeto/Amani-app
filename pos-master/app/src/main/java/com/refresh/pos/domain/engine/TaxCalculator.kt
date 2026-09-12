package com.refresh.pos.domain.engine

import java.math.BigDecimal
import java.math.RoundingMode

object TaxCalculator {

    const val DEFAULT_VAT_RATE_PERCENT = 16.0 // 16% standard VAT

    data class TaxBreakdown(
        val subtotalExclTax: Double,
        val taxAmount: Double,
        val totalInclTax: Double
    )

    /**
     * Calculates tax assuming retail prices are Tax-Inclusive.
     * Subtotal = Total / (1 + Rate)
     * Tax = Total - Subtotal
     */
    fun calculateInclusive(totalAmount: Double, ratePercent: Double = DEFAULT_VAT_RATE_PERCENT): TaxBreakdown {
        if (totalAmount <= 0.0 || ratePercent <= 0.0) {
            return TaxBreakdown(totalAmount, 0.0, totalAmount)
        }
        val rateFactor = BigDecimal.valueOf(1.0 + (ratePercent / 100.0))
        val totalBd = BigDecimal.valueOf(totalAmount)
        val subtotalBd = totalBd.divide(rateFactor, 2, RoundingMode.HALF_UP)
        val taxBd = totalBd.subtract(subtotalBd)

        return TaxBreakdown(
            subtotalExclTax = subtotalBd.toDouble(),
            taxAmount = taxBd.toDouble(),
            totalInclTax = totalBd.setScale(2, RoundingMode.HALF_UP).toDouble()
        )
    }

    /**
     * Calculates tax assuming prices are Tax-Exclusive.
     * Tax = Subtotal * Rate
     * Total = Subtotal + Tax
     */
    fun calculateExclusive(subtotalAmount: Double, ratePercent: Double = DEFAULT_VAT_RATE_PERCENT): TaxBreakdown {
        if (subtotalAmount <= 0.0 || ratePercent <= 0.0) {
            return TaxBreakdown(subtotalAmount, 0.0, subtotalAmount)
        }
        val subtotalBd = BigDecimal.valueOf(subtotalAmount)
        val taxBd = subtotalBd.multiply(BigDecimal.valueOf(ratePercent / 100.0)).setScale(2, RoundingMode.HALF_UP)
        val totalBd = subtotalBd.add(taxBd)

        return TaxBreakdown(
            subtotalExclTax = subtotalBd.setScale(2, RoundingMode.HALF_UP).toDouble(),
            taxAmount = taxBd.toDouble(),
            totalInclTax = totalBd.toDouble()
        )
    }
}
