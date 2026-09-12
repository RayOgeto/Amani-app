package com.refresh.pos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.refresh.pos.data.local.dao.CashierShiftDao
import com.refresh.pos.data.local.dao.CustomerDao
import com.refresh.pos.data.local.dao.ProductDao
import com.refresh.pos.data.local.dao.SaleDao
import com.refresh.pos.data.local.dao.StockDao
import com.refresh.pos.data.local.entity.CashierShiftEntity
import com.refresh.pos.data.local.entity.CustomerEntity
import com.refresh.pos.data.local.entity.ProductEntity
import com.refresh.pos.data.local.entity.SaleEntity
import com.refresh.pos.data.local.entity.SaleLineItemEntity
import com.refresh.pos.data.local.entity.StockLotEntity

@Database(
    entities = [
        ProductEntity::class,
        StockLotEntity::class,
        SaleEntity::class,
        SaleLineItemEntity::class,
        CashierShiftEntity::class,
        CustomerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun stockDao(): StockDao
    abstract fun saleDao(): SaleDao
    abstract fun cashierShiftDao(): CashierShiftDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: PosDatabase? = null

        fun getDatabase(context: Context): PosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    "amani_pos.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                        }
                    })
                    .build()
                INSTANCE = instance

                // Seed products & initial customers if needed
                DatabaseSeeder.seedIfEmpty(context, instance.productDao(), instance.customerDao())

                instance
            }
        }
    }
}
