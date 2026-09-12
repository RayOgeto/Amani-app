package com.refresh.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_line_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["sale_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sale_id"),
        Index("product_id")
    ]
)
data class SaleLineItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "sale_id")
    val saleId: Long,

    @ColumnInfo(name = "product_id")
    val productId: Long,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "barcode")
    val barcode: String = "",

    @ColumnInfo(name = "quantity")
    val quantity: Double, // Double for weighed items (e.g. 1.25 kg)

    @ColumnInfo(name = "unit_of_measure")
    val unitOfMeasure: String = "PCS",

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,

    @ColumnInfo(name = "total_price")
    val totalPrice: Double
)
