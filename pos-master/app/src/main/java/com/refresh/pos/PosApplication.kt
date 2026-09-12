package com.refresh.pos

import android.app.Application
import com.refresh.pos.data.local.PosDatabase
import com.refresh.pos.data.repository.CustomerRepository
import com.refresh.pos.data.repository.ProductRepository
import com.refresh.pos.data.repository.SaleRepository
import com.refresh.pos.data.repository.ShiftRepository
import com.refresh.pos.printer.EscPosPrinterService

class PosApplication : Application() {

    val database: PosDatabase by lazy {
        PosDatabase.getDatabase(this)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(database.productDao(), database.stockDao())
    }

    val customerRepository: CustomerRepository by lazy {
        CustomerRepository(database.customerDao())
    }

    val saleRepository: SaleRepository by lazy {
        SaleRepository(database.saleDao(), database.productDao(), database.customerDao())
    }

    val shiftRepository: ShiftRepository by lazy {
        ShiftRepository(database.cashierShiftDao())
    }

    val printerService: EscPosPrinterService by lazy {
        EscPosPrinterService(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PosApplication
            private set
    }
}
