package com.example.moneymanagement.data.repository

import androidx.lifecycle.LiveData
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.data.model.CategoryDao
import com.example.moneymanagement.data.model.MonthlyStatistic
import com.example.moneymanagement.data.model.TransactionDao
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.data.model.Transactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    fun getAllTransactions(userId: String): LiveData<List<TransactionWithCategory>> {
        return transactionDao.getAllTransactionsWithCategory(userId)
    }

//    fun getTransactionsByDate(date: String): LiveData<List<TransactionWithCategory>> {
//        return transactionDao.getTransactionsByDate(date)
//    }

    fun getTransactionsByType(userId: String, type: TransactionType): LiveData<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsByType(userId, type)
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

    suspend fun getTotalIncome(userId: String): Double {
        return transactionDao.getTotalByType(userId, TransactionType.INCOME) ?: 0.0
    }

    suspend fun getTotalExpense(userId: String): Double {
        return transactionDao.getTotalByType(userId, TransactionType.EXPENSE) ?: 0.0
    }

    suspend fun getBalance(userId: String): Double {
        return getTotalIncome(userId) - getTotalExpense(userId)
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
        withContext(Dispatchers.IO) {
            categoryDao.insertCategory(category)
        }
    }

    suspend fun insertCategories(categories: List<Category>) {
        withContext(Dispatchers.IO) {
            categoryDao.insertCategories(categories)
        }
    }

    suspend fun deleteCategory(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.deleteCategory(category)
        }
    }

    suspend fun getNextCategoryId(): Int {
        return withContext(Dispatchers.IO) {
            val allCategories = categoryDao.getAllCategories().value ?: emptyList()
            (allCategories.maxOfOrNull { it.id } ?: 0) + 1
        }
    }


    fun getTransactionsByCategory(userId: String, type: TransactionType): LiveData<List<CategoryTotal>> {
        return transactionDao.getTransactionsByCategory(userId, type)
    }

//    fun getMonthlyTotalByType(type: TransactionType, year: Int, month: Int): LiveData<Double> {
//        val startDate = String.format("%04d-%02d-01", year, month)
//        val endDate = String.format("%04d-%02d-31", year, month)
//        return transactionDao.getMonthlyTotalByType(type, startDate, endDate)
//    }

    data class CategoryTotal(
        val categoryName: String,
        val total: Double
    )



    // Statistics
//    suspend fun getMonthlyStatistics(startDate: String, endDate: String): List<MonthlyStatistic> {
//        return transactionDao.getMonthlyStatistics(startDate, endDate)
//    }
}
