package com.refresh.pos.ui.report

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.refresh.pos.PosApplication
import com.refresh.pos.data.local.relation.SaleWithLineItems
import com.refresh.pos.domain.model.PeriodFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<PosApplication>()
    private val saleRepo = app.saleRepository
    private val customerRepo = app.customerRepository
    private val productRepo = app.productRepository

    val selectedPeriod = MutableStateFlow(PeriodFilter.DAILY)

    val sales: StateFlow<List<SaleWithLineItems>> = selectedPeriod
        .flatMapLatest { period ->
            val bounds = calculatePeriodBounds(period)
            saleRepo.getSalesBetween(bounds.first, bounds.second)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Double> = sales.map { list ->
        list.sumOf { it.sale.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOrders: StateFlow<Int> = sales.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cashRevenue: StateFlow<Double> = sales.map { list ->
        list.filter { it.sale.paymentMethod == "CASH" }.sumOf { it.sale.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val digitalRevenue: StateFlow<Double> = sales.map { list ->
        list.filter { it.sale.paymentMethod != "CASH" }.sumOf { it.sale.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDebt: StateFlow<Double> = customerRepo.totalOutstandingDebt
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val debtors: StateFlow<List<com.refresh.pos.data.local.entity.CustomerEntity>> = customerRepo.debtors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringLots: StateFlow<List<com.refresh.pos.data.local.entity.StockLotEntity>> = productRepo.getExpiringLots(withinDays = 7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordCustomerDebtPayment(customerId: Long, amount: Double) {
        viewModelScope.launch {
            customerRepo.recordDebtPayment(customerId, amount)
        }
    }

    fun setPeriod(period: PeriodFilter) {
        selectedPeriod.value = period
    }

    fun clearHistory() {
        viewModelScope.launch {
            saleRepo.clearHistory()
        }
    }

    private fun calculatePeriodBounds(period: PeriodFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis

        when (period) {
            PeriodFilter.DAILY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            PeriodFilter.WEEKLY -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            PeriodFilter.MONTHLY -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            PeriodFilter.YEARLY -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
        }
        val start = cal.timeInMillis
        return Pair(start, end)
    }
}
