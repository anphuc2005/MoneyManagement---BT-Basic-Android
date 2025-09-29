package com.example.moneymanagement.data.repository

import androidx.lifecycle.LiveData
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.data.model.CategoryDao
import com.example.moneymanagement.data.model.MonthlyStatistic
import com.example.moneymanagement.data.model.TransactionDao
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.data.model.Transactions

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    fun getAllTransactions(): LiveData<List<TransactionWithCategory>> {
        return transactionDao.getAllTransactionsWithCategory()
    }

//    fun getTransactionsByDate(date: String): LiveData<List<TransactionWithCategory>> {
//        return transactionDao.getTransactionsByDate(date)
//    }

    fun getTransactionsByType(type: TransactionType): LiveData<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsByType(type)
    }

    suspend fun insertTransaction(transaction: Transactions): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transactions) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transactions) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTotalIncome(): Double {
        return transactionDao.getTotalByType(TransactionType.INCOME) ?: 0.0
    }

    suspend fun getTotalExpense(): Double {
        return transactionDao.getTotalByType(TransactionType.EXPENSE) ?: 0.0
    }

    suspend fun getBalance(): Double {
        return getTotalIncome() - getTotalExpense()
    }

    fun getAllCategories(): LiveData<List<Category>> {
        return categoryDao.getAllCategories()
    }

    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>> {
        return categoryDao.getCategoriesByType(type)
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    // Statistics
//    suspend fun getMonthlyStatistics(startDate: String, endDate: String): List<MonthlyStatistic> {
//        return transactionDao.getMonthlyStatistics(startDate, endDate)
//    }
}