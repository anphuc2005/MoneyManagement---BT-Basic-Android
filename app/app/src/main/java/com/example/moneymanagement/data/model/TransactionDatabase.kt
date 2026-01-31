package com.example.moneymanagement.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moneymanagement.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transactions::class, Category::class],
    version = 2, // Tăng version lên 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id TEXT NOT NULL,
                        category_id INTEGER NOT NULL,
                        transaction_name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        date TEXT NOT NULL,
                        note TEXT NOT NULL,
                        type TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("CREATE INDEX index_transactions_new_category_id ON transactions_new(category_id)")
                database.execSQL("CREATE INDEX index_transactions_new_user_id ON transactions_new(user_id)")

                database.execSQL("""
                    INSERT INTO transactions_new (id, user_id, category_id, transaction_name, amount, date, note, type, createdAt)
                    SELECT id, CAST(user_id AS TEXT), category_id, transaction_name, amount, date, note, type, createdAt
                    FROM transactions
                """.trimIndent())

                database.execSQL("DROP TABLE transactions")

                database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
            }
        }

        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                )
                    .addMigrations(MIGRATION_1_2)
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
                populateDatabase(database.categoryDao(), context)
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao, context: Context) {
            val defaultCategories = listOf(
                Category(1, context.getString(R.string.category_salary), "salary", TransactionType.INCOME),
                Category(2, context.getString(R.string.category_bonus), "bonus", TransactionType.INCOME),
                Category(3, context.getString(R.string.category_investment), "investment", TransactionType.INCOME),
                Category(4, context.getString(R.string.category_other_money), "other_money", TransactionType.INCOME),
                Category(5, context.getString(R.string.category_food), "food", TransactionType.EXPENSE),
                Category(6, context.getString(R.string.category_coffee), "coffee", TransactionType.EXPENSE),
                Category(7, context.getString(R.string.category_gas), "gas", TransactionType.EXPENSE),
                Category(8, context.getString(R.string.category_fashion), "fashion", TransactionType.EXPENSE),
                Category(9, context.getString(R.string.category_entertainment), "entertainment", TransactionType.EXPENSE),
                Category(10, context.getString(R.string.category_pet), "pet", TransactionType.EXPENSE),
                Category(11, context.getString(R.string.category_education), "education", TransactionType.EXPENSE),
                Category(12, context.getString(R.string.category_medical), "medical", TransactionType.EXPENSE),
                Category(13, context.getString(R.string.category_travel), "travel", TransactionType.EXPENSE),
                Category(14, context.getString(R.string.category_bill), "bill", TransactionType.EXPENSE),
                Category(15, context.getString(R.string.category_gift), "gift", TransactionType.EXPENSE)
            )

            categoryDao.insertCategories(defaultCategories)
        }
    }
}