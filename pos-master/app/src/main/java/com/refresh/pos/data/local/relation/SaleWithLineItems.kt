package com.refresh.pos.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.refresh.pos.data.local.entity.SaleEntity
import com.refresh.pos.data.local.entity.SaleLineItemEntity

data class SaleWithLineItems(
    @Embedded
    val sale: SaleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "sale_id"
    )
    val lineItems: List<SaleLineItemEntity>
)
