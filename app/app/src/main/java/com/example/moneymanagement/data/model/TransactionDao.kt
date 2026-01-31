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
    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY createdAt DESC")
    fun getAllTransactionsWithCategory(userId: String): LiveData<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND date = :date ORDER BY createdAt DESC")
    fun getTransactionsByDate(userId: String, date: String): LiveData<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND type = :type ORDER BY createdAt DESC")
    fun getTransactionsByType(userId: String, type: TransactionType): LiveData<List<TransactionWithCategory>>

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = :type")
    suspend fun getTotalByType(userId: String, type: TransactionType): Double?

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

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getTransactionsByDateRange(userId: String, type: TransactionType, startDate: String, endDate: String): LiveData<List<Transactions>>

    @Query("""
        SELECT c.type_name as categoryName, SUM(t.amount) as total 
        FROM transactions t 
        INNER JOIN categories c ON t.category_id = c.id
        WHERE t.user_id = :userId AND t.type = :type 
        GROUP BY t.category_id, c.type_name
        ORDER BY total DESC
    """)
    fun getTransactionsByCategory(userId: String, type: TransactionType): LiveData<List<TransactionRepository.CategoryTotal>>

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    fun getMonthlyTotalByType(userId: String, type: TransactionType, startDate: String, endDate: String): LiveData<Double>
}