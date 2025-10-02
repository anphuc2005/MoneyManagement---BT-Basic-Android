package com.example.moneymanagement.data.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.moneymanagement.data.repository.TransactionRepository

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactionsWithCategory(): LiveData<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY createdAt DESC")
    fun getTransactionsByDate(date: String): LiveData<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY createdAt DESC")
    fun getTransactionsByType(type: TransactionType): LiveData<List<TransactionWithCategory>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    suspend fun getTotalByType(type: TransactionType): Double?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transactions?

    @Insert
    suspend fun insertTransaction(transaction: Transactions): Long

    @Update
    suspend fun updateTransaction(transaction: Transactions)

    @Delete
    suspend fun deleteTransaction(transaction: Transactions)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getTransactionsByDateRange(type: TransactionType, startDate: String, endDate: String): LiveData<List<Transactions>>

    @Query("""
        SELECT c.type_name as categoryName, SUM(t.amount) as total 
        FROM transactions t 
        INNER JOIN categories c ON t.category_id = c.id
        WHERE t.type = :type 
        GROUP BY t.category_id, c.type_name
        ORDER BY total DESC
    """)
    fun getTransactionsByCategory(type: TransactionType): LiveData<List<TransactionRepository.CategoryTotal>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    fun getMonthlyTotalByType(type: TransactionType, startDate: String, endDate: String): LiveData<Double>

    // Thống kê theo tháng
//    @Query("""
//        SELECT DATE(date, 'start of month') as month,
//               type,
//               SUM(amount) as total
//        FROM transactions
//        WHERE date BETWEEN :startDate AND :endDate
//        GROUP BY month, type
//        ORDER BY month DESC
//    """)
//    suspend fun getMonthlyStatistics(startDate: String, endDate: String): List<MonthlyStatistic>
}