package com.refresh.pos.ui.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.refresh.pos.PosApplication
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.data.local.entity.StockLotEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<PosApplication>()
    private val repo = app.productRepository

    val searchQuery = MutableStateFlow("")

    val products: StateFlow<List<ProductEntity>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repo.allProducts
            } else {
                repo.search(query.trim())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repo.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringLots: StateFlow<List<StockLotEntity>> = repo.getExpiringLots(withinDays = 7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProduct(
        name: String,
        barcode: String,
        price: Double,
        cost: Double = 0.0,
        category: String = "General",
        unitOfMeasure: String = "PCS",
        isWeighed: Boolean = false,
        pluCode: String = "",
        initialStock: Double = 0.0,
        existingId: Long = 0
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = existingId,
                name = name.trim(),
                barcode = barcode.trim(),
                unitPrice = price,
                costPrice = cost,
                category = category.trim(),
                unitOfMeasure = unitOfMeasure.trim().uppercase(),
                isWeighed = isWeighed,
                pluCode = pluCode.trim(),
                stockQuantity = initialStock
            )
            repo.saveProduct(product)
        }
    }

    fun addStockLot(
        productId: Long,
        quantity: Double,
        cost: Double,
        expiryDate: String = ""
    ) {
        viewModelScope.launch {
            val now = Date()
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(now)
            val expiryTimestamp = if (expiryDate.isNotBlank()) {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiryDate)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            } else {
                0L
            }

            repo.addStockLot(
                productId = productId,
                quantity = quantity,
                cost = cost,
                dateAdded = dateStr,
                expiryDate = expiryDate,
                expiryTimestamp = expiryTimestamp
            )
        }
    }

    fun toggleSuspend(product: ProductEntity) {
        viewModelScope.launch {
            repo.setSuspended(product.id, !product.isSuspended)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repo.delete(product)
        }
    }
}
