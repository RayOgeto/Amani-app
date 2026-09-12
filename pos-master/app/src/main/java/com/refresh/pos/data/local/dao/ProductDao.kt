package com.refresh.pos.data.local.dao

import androidx.room.*
import com.refresh.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE is_suspended = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products WHERE is_suspended = 0 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE is_suspended = 0 AND category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE is_suspended = 0 AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR plu_code LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE (barcode = :code OR plu_code = :code) AND is_suspended = 0 LIMIT 1")
    suspend fun getByBarcodeOrPlu(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE is_suspended = 0 AND stock_quantity <= 5.0 ORDER BY stock_quantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>): List<Long>

    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET stock_quantity = stock_quantity + :delta WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Double)

    @Query("UPDATE products SET is_suspended = :suspended WHERE id = :productId")
    suspend fun setSuspended(productId: Long, suspended: Boolean)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Delete
    suspend fun delete(product: ProductEntity)
}
