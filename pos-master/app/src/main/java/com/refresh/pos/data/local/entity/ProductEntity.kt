package com.refresh.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = false),
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["plu_code"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,

    @ColumnInfo(name = "cost_price")
    val costPrice: Double = 0.0,

    @ColumnInfo(name = "category")
    val category: String = "General",

    @ColumnInfo(name = "unit_of_measure")
    val unitOfMeasure: String = "PCS", // KG, G, PCS, FUNGU, PKT, TRAY

    @ColumnInfo(name = "is_weighed")
    val isWeighed: Boolean = false,

    @ColumnInfo(name = "plu_code")
    val pluCode: String = "",

    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Double = 0.0,

    @ColumnInfo(name = "is_suspended")
    val isSuspended: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
