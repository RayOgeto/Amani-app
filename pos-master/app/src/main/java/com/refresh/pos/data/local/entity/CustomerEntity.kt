package com.refresh.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index("name"),
        Index("phone")
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String = "",

    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double = 5000.0,

    @ColumnInfo(name = "current_debt")
    val currentDebt: Double = 0.0,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
