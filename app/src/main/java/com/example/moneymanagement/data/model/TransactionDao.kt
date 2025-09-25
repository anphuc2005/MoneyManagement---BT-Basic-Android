package com.example.moneymanagement.data.model

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

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

    // Thống kê theo tháng
    @Query("""
        SELECT DATE(date, 'start of month') as month, 
               type, 
               SUM(amount) as total 
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY month, type 
        ORDER BY month DESC
    """)
    suspend fun getMonthlyStatistics(startDate: String, endDate: String): List<MonthlyStatistic>
}