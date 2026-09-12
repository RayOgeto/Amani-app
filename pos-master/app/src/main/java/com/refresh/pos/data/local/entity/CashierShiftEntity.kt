package com.refresh.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cashier_shifts")
data class CashierShiftEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "cashier_name")
    val cashierName: String = "Cashier 1",

    @ColumnInfo(name = "start_time")
    val startTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "opening_float")
    val openingFloat: Double = 0.0,

    @ColumnInfo(name = "cash_sales")
    val cashSales: Double = 0.0,

    @ColumnInfo(name = "digital_sales")
    val digitalSales: Double = 0.0,

    @ColumnInfo(name = "total_sales")
    val totalSales: Double = 0.0,

    @ColumnInfo(name = "total_orders")
    val totalOrders: Int = 0,

    @ColumnInfo(name = "status")
    val status: String = "OPEN" // OPEN, CLOSED
)
