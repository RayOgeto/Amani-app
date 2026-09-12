package com.refresh.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.refresh.pos.data.local.entity.StockLotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: StockLotEntity): Long

    @Query("SELECT * FROM stock_lots WHERE product_id = :productId ORDER BY timestamp DESC")
    fun getLotsByProduct(productId: Long): Flow<List<StockLotEntity>>

    @Query("SELECT * FROM stock_lots ORDER BY timestamp DESC")
    fun getAllLots(): Flow<List<StockLotEntity>>

    @Query("SELECT * FROM stock_lots WHERE expiry_timestamp > 0 AND expiry_timestamp <= :untilTimestamp ORDER BY expiry_timestamp ASC")
    fun getExpiringLots(untilTimestamp: Long): Flow<List<StockLotEntity>>

    @Query("SELECT SUM(quantity) FROM stock_lots WHERE product_id = :productId")
    suspend fun getTotalStockForProduct(productId: Long): Double?
}
