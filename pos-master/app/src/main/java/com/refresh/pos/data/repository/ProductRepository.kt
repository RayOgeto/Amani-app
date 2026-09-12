package com.refresh.pos.data.repository

import com.refresh.pos.data.local.dao.ProductDao
import com.refresh.pos.data.local.dao.StockDao
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.data.local.entity.StockLotEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productDao: ProductDao,
    private val stockDao: StockDao
) {

    val activeProducts: Flow<List<ProductEntity>> = productDao.getAllActive()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAll()
    val categories: Flow<List<String>> = productDao.getAllCategories()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    fun search(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }

    fun getByCategory(category: String): Flow<List<ProductEntity>> {
        return productDao.getByCategory(category)
    }

    fun getExpiringLots(withinDays: Int = 7): Flow<List<StockLotEntity>> {
        val targetTimestamp = System.currentTimeMillis() + (withinDays * 24 * 60 * 60 * 1000L)
        return stockDao.getExpiringLots(targetTimestamp)
    }

    suspend fun getById(id: Long): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getById(id)
    }

    suspend fun getByBarcodeOrPlu(code: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getByBarcodeOrPlu(code)
    }

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insert(product)
    }

    suspend fun addStockLot(
        productId: Long,
        quantity: Double,
        cost: Double,
        dateAdded: String,
        expiryDate: String = "",
        expiryTimestamp: Long = 0L
    ): Long = withContext(Dispatchers.IO) {
        val lot = StockLotEntity(
            productId = productId,
            quantity = quantity,
            cost = cost,
            dateAdded = dateAdded,
            expiryDate = expiryDate,
            expiryTimestamp = expiryTimestamp
        )
        val lotId = stockDao.insertLot(lot)
        productDao.adjustStock(productId, quantity)
        lotId
    }

    suspend fun setSuspended(productId: Long, suspended: Boolean) = withContext(Dispatchers.IO) {
        productDao.setSuspended(productId, suspended)
    }

    suspend fun delete(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.delete(product)
    }
}
