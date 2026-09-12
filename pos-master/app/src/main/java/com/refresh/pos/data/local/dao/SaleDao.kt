package com.refresh.pos.data.local.dao

import androidx.room.*
import com.refresh.pos.data.local.entity.SaleEntity
import com.refresh.pos.data.local.entity.SaleLineItemEntity
import com.refresh.pos.data.local.relation.SaleWithLineItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItems(items: List<SaleLineItemEntity>): List<Long>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId LIMIT 1")
    suspend fun getSaleWithItemsById(saleId: Long): SaleWithLineItems?

    @Transaction
    @Query("SELECT * FROM sales WHERE status = 'ENDED' ORDER BY timestamp DESC")
    fun getAllCompletedSales(): Flow<List<SaleWithLineItems>>

    @Transaction
    @Query("""
        SELECT * FROM sales 
        WHERE status = 'ENDED' AND timestamp >= :startTimestamp AND timestamp <= :endTimestamp 
        ORDER BY timestamp DESC
    """)
    fun getSalesBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<SaleWithLineItems>>

    @Query("SELECT SUM(total) FROM sales WHERE status = 'ENDED' AND timestamp >= :fromTimestamp")
    fun getTotalRevenueSince(fromTimestamp: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales WHERE status = 'ENDED' AND timestamp >= :fromTimestamp")
    fun getOrderCountSince(fromTimestamp: Long): Flow<Int>

    @Query("SELECT SUM(total) FROM sales WHERE status = 'ENDED' AND payment_method = :method AND timestamp >= :fromTimestamp")
    fun getPaymentMethodTotal(method: String, fromTimestamp: Long): Flow<Double?>

    @Query("DELETE FROM sales")
    suspend fun clearSales()

    @Query("DELETE FROM sale_line_items")
    suspend fun clearLineItems()
}
