package com.refresh.pos.data.local

import android.content.Context
import android.util.Log
import com.refresh.pos.R
import com.refresh.pos.data.local.dao.CustomerDao
import com.refresh.pos.data.local.dao.ProductDao
import com.refresh.pos.data.local.entity.CustomerEntity
import com.refresh.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

object DatabaseSeeder {

    private const val TAG = "DatabaseSeeder"

    fun seedIfEmpty(context: Context, productDao: ProductDao, customerDao: CustomerDao) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (productDao.count() == 0) {
                    val inputStream = context.resources.openRawResource(R.raw.products)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val products = mutableListOf<ProductEntity>()

                    reader.useLines { lines ->
                        lines.forEach { line ->
                            val parts = line.split(",")
                            if (parts.size >= 3) {
                                val barcode = parts[0].trim()
                                val name = parts[1].trim()
                                val price = parts[2].trim().toDoubleOrNull() ?: 0.0
                                val category = if (parts.size > 3) parts[3].trim() else "General"
                                val uom = if (parts.size > 4) parts[4].trim() else "PCS"
                                val isWeighed = if (parts.size > 5) parts[5].trim() == "1" else false
                                val plu = if (parts.size > 6) parts[6].trim() else ""

                                products.add(
                                    ProductEntity(
                                        name = name,
                                        barcode = barcode,
                                        unitPrice = price,
                                        costPrice = price * 0.8, // 20% margin
                                        category = category,
                                        unitOfMeasure = uom,
                                        isWeighed = isWeighed,
                                        pluCode = plu,
                                        stockQuantity = if (isWeighed) 50.0 else 30.0
                                    )
                                )
                            }
                        }
                    }

                    if (products.isNotEmpty()) {
                        productDao.insertAll(products)
                        Log.d(TAG, "Successfully seeded ${products.size} Kenyan grocery products.")
                    }
                }

                // Seed sample neighborhood credit customers
                val customers = listOf(
                    CustomerEntity(name = "Mama Brian", phone = "0712345678", currentDebt = 350.0),
                    CustomerEntity(name = "John Ochieng", phone = "0722334455", currentDebt = 0.0),
                    CustomerEntity(name = "Pastor Mwangi", phone = "0733998877", currentDebt = 120.0)
                )
                for (cust in customers) {
                    customerDao.insert(cust)
                }
                Log.d(TAG, "Successfully seeded sample neighborhood credit customers.")

            } catch (e: Exception) {
                Log.e(TAG, "Error seeding database", e)
            }
        }
    }
}
