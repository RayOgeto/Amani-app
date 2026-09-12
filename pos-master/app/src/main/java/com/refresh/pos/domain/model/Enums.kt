package com.refresh.pos.domain.model

enum class PaymentMethod(val displayName: String) {
    CASH("Cash (Pesa Taslimu)"),
    MPESA_TILL("M-Pesa Buy Goods"),
    MPESA_PAYBILL("M-Pesa Paybill"),
    STORE_CREDIT("Duka Credit (Deni)"),
    CARD("Bank Card"),
    SPLIT("Split Tender");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CASH
        }
    }
}

enum class PeriodFilter(val title: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}
