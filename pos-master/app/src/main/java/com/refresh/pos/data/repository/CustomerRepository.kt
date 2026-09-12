package com.refresh.pos.data.repository

import com.refresh.pos.data.local.dao.CustomerDao
import com.refresh.pos.data.local.entity.CustomerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CustomerRepository(private val customerDao: CustomerDao) {

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAll()
    val debtors: Flow<List<CustomerEntity>> = customerDao.getCustomersWithDebt()
    val totalOutstandingDebt: Flow<Double?> = customerDao.getTotalOutstandingDebt()

    fun search(query: String): Flow<List<CustomerEntity>> {
        return customerDao.search(query)
    }

    suspend fun getById(id: Long): CustomerEntity? = withContext(Dispatchers.IO) {
        customerDao.getById(id)
    }

    suspend fun saveCustomer(customer: CustomerEntity): Long = withContext(Dispatchers.IO) {
        customerDao.insert(customer)
    }

    suspend fun addDebt(customerId: Long, amount: Double) = withContext(Dispatchers.IO) {
        customerDao.adjustDebt(customerId, amount)
    }

    suspend fun recordDebtPayment(customerId: Long, paymentAmount: Double) = withContext(Dispatchers.IO) {
        customerDao.adjustDebt(customerId, -paymentAmount)
    }
}
