package com.refresh.pos.data.repository

import com.refresh.pos.data.local.dao.CashierShiftDao
import com.refresh.pos.data.local.entity.CashierShiftEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ShiftRepository(private val shiftDao: CashierShiftDao) {

    val activeShiftFlow: Flow<CashierShiftEntity?> = shiftDao.getActiveShiftFlow()
    val allShifts: Flow<List<CashierShiftEntity>> = shiftDao.getAllShifts()

    suspend fun getActiveShift(): CashierShiftEntity? = withContext(Dispatchers.IO) {
        shiftDao.getActiveShift()
    }

    suspend fun openShift(cashierName: String, openingFloat: Double): Long = withContext(Dispatchers.IO) {
        val shift = CashierShiftEntity(
            cashierName = cashierName,
            openingFloat = openingFloat,
            status = "OPEN"
        )
        shiftDao.insert(shift)
    }

    suspend fun closeShift(
        shift: CashierShiftEntity,
        cashSales: Double,
        digitalSales: Double,
        totalSales: Double,
        totalOrders: Int
    ) = withContext(Dispatchers.IO) {
        val closedShift = shift.copy(
            endTime = System.currentTimeMillis(),
            cashSales = cashSales,
            digitalSales = digitalSales,
            totalSales = totalSales,
            totalOrders = totalOrders,
            status = "CLOSED"
        )
        shiftDao.update(closedShift)
    }
}
