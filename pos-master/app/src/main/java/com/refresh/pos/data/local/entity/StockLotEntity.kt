package com.refresh.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_lots",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("product_id"),
        Index("expiry_timestamp")
    ]
)
data class StockLotEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "product_id")
    val productId: Long,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "cost")
    val cost: Double,

    @ColumnInfo(name = "date_added")
    val dateAdded: String,

    @ColumnInfo(name = "expiry_date")
    val expiryDate: String = "",

    @ColumnInfo(name = "expiry_timestamp")
    val expiryTimestamp: Long = 0L,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
