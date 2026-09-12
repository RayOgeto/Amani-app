package com.refresh.pos.domain.model

import com.refresh.pos.data.local.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    var quantity: Double = 1.0
) {
    val subtotal: Double
        get() = product.unitPrice * quantity
}
