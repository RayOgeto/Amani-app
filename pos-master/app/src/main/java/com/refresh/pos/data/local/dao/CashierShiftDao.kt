package com.refresh.pos.data.local.dao

import androidx.room.*
import com.refresh.pos.data.local.entity.CashierShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashierShiftDao {

    @Query("SELECT * FROM cashier_shifts WHERE status = 'OPEN' ORDER BY start_time DESC LIMIT 1")
    fun getActiveShiftFlow(): Flow<CashierShiftEntity?>

    @Query("SELECT * FROM cashier_shifts WHERE status = 'OPEN' ORDER BY start_time DESC LIMIT 1")
    suspend fun getActiveShift(): CashierShiftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shift: CashierShiftEntity): Long

    @Update
    suspend fun update(shift: CashierShiftEntity)

    @Query("SELECT * FROM cashier_shifts ORDER BY start_time DESC")
    fun getAllShifts(): Flow<List<CashierShiftEntity>>
}
