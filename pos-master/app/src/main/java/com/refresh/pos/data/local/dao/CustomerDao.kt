package com.refresh.pos.data.local.dao

import androidx.room.*
import com.refresh.pos.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE current_debt > 0 ORDER BY current_debt DESC")
    fun getCustomersWithDebt(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("UPDATE customers SET current_debt = current_debt + :delta, last_updated = :timestamp WHERE id = :customerId")
    suspend fun adjustDebt(customerId: Long, delta: Double, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT SUM(current_debt) FROM customers WHERE current_debt > 0")
    fun getTotalOutstandingDebt(): Flow<Double?>
}
