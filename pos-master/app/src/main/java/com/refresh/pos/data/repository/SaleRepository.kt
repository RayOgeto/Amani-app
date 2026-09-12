package com.refresh.pos.data.repository

import com.refresh.pos.data.local.dao.CustomerDao
import com.refresh.pos.data.local.dao.ProductDao
import com.refresh.pos.data.local.dao.SaleDao
import com.refresh.pos.data.local.entity.SaleEntity
import com.refresh.pos.data.local.entity.SaleLineItemEntity
import com.refresh.pos.data.local.relation.SaleWithLineItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SaleRepository(
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val customerDao: CustomerDao
) {

    val allCompletedSales: Flow<List<SaleWithLineItems>> = saleDao.getAllCompletedSales()

    fun getSalesBetween(start: Long, end: Long): Flow<List<SaleWithLineItems>> {
        return saleDao.getSalesBetween(start, end)
    }

    fun getRevenueSince(start: Long): Flow<Double?> {
        return saleDao.getTotalRevenueSince(start)
    }

    fun getOrderCountSince(start: Long): Flow<Int> {
        return saleDao.getOrderCountSince(start)
    }

    suspend fun getSaleById(id: Long): SaleWithLineItems? = withContext(Dispatchers.IO) {
        saleDao.getSaleWithItemsById(id)
    }

    suspend fun completeSale(
        sale: SaleEntity,
        items: List<SaleLineItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        // Insert sale header
        val saleId = saleDao.insertSale(sale)

        // Assign saleId to line items and insert
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        saleDao.insertLineItems(itemsWithSaleId)

        // Deduct inventory quantities (supports decimal quantities e.g. 1.25 kg)
        for (item in itemsWithSaleId) {
            productDao.adjustStock(item.productId, -item.quantity)
        }

        // If charged to Customer Credit Tab / Deni, update customer balance
        if (sale.paymentMethod == "STORE_CREDIT" && sale.customerId != null && sale.customerId > 0) {
            customerDao.adjustDebt(sale.customerId, sale.total)
        }

        saleId
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        saleDao.clearSales()
        saleDao.clearLineItems()
    }
}
