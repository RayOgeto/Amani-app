package com.refresh.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index("timestamp"),
        Index("status"),
        Index("payment_method"),
        Index("customer_id")
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: String,

    @ColumnInfo(name = "end_time")
    val endTime: String,

    @ColumnInfo(name = "status")
    val status: String = "ENDED", // ENDED, CANCELED, ON_PROCESS

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "CASH", // CASH, MPESA_TILL, MPESA_PAYBILL, STORE_CREDIT, CARD

    @ColumnInfo(name = "subtotal")
    val subtotal: Double = 0.0,

    @ColumnInfo(name = "tax")
    val tax: Double = 0.0,

    @ColumnInfo(name = "discount")
    val discount: Double = 0.0,

    @ColumnInfo(name = "total")
    val total: Double = 0.0,

    @ColumnInfo(name = "amount_tendered")
    val amountTendered: Double = 0.0,

    @ColumnInfo(name = "change_amount")
    val changeAmount: Double = 0.0,

    @ColumnInfo(name = "payment_reference")
    val paymentReference: String = "",

    @ColumnInfo(name = "customer_id")
    val customerId: Long? = null,

    @ColumnInfo(name = "customer_name")
    val customerName: String = "",

    @ColumnInfo(name = "cashier_name")
    val cashierName: String = "Cashier",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
