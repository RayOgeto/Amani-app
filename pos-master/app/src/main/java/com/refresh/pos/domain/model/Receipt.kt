package com.refresh.pos.domain.model

data class Receipt(
    val receiptNumber: String,
    val storeName: String = "AMANI GROCERY & MINI-MART",
    val storeAddress: String = "Biashara Street, Nairobi",
    val storePhone: String = "Tel: +254 712 345 678",
    val tillNumber: String = "Buy Goods Till: 5432100",
    val cashierName: String,
    val dateString: String,
    val customerName: String = "",
    val items: List<ReceiptItem>,
    val subtotal: Double,
    val taxRatePercent: Double = 16.0,
    val taxAmount: Double,
    val discountAmount: Double = 0.0,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val amountTendered: Double,
    val changeAmount: Double,
    val paymentReference: String = ""
)

data class ReceiptItem(
    val name: String,
    val quantity: Double,
    val unitOfMeasure: String = "PCS",
    val unitPrice: Double,
    val total: Double
)
