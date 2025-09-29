package com.example.moneymanagement.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transactions::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                populateDatabase(database.categoryDao())
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(1, "Lương", "salary", TransactionType.INCOME),
                Category(2, "Thưởng", "bonus", TransactionType.INCOME),
                Category(3, "Đầu tư", "investment", TransactionType.INCOME),
                Category(4, "Tiền khác", "other_money", TransactionType.INCOME),
                Category(5, "Thực phẩm", "food", TransactionType.EXPENSE),
                Category(6, "Cà phê", "coffee", TransactionType.EXPENSE),
                Category(7, "Di chuyển", "gas", TransactionType.EXPENSE),
                Category(8, "Thời trang", "fashion", TransactionType.EXPENSE),
                Category(9, "Giải trí", "entertainment", TransactionType.EXPENSE),
                Category(10, "Thú cưng", "pet", TransactionType.EXPENSE),
                Category(11, "Giáo dục", "education", TransactionType.EXPENSE),
                Category(12, "Y tế", "medical", TransactionType.EXPENSE),
                Category(13, "Du lịch", "travel", TransactionType.EXPENSE),
                Category(14, "Hoá đơn nước", "bill", TransactionType.EXPENSE),
                Category(15, "Quà tặng", "gift", TransactionType.EXPENSE)
            )

            categoryDao.insertCategories(defaultCategories)
        }
    }
}