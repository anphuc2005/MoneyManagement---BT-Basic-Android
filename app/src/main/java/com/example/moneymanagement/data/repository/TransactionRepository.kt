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


    fun getTransactionsByType(
        userId: String,
        type: TransactionType
    ): LiveData<List<TransactionWithCategory>> {
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


    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.insertCategory(category)
        }
    }


    suspend fun deleteCategory(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.deleteCategory(category)
        }
    }


    fun getTransactionsByCategory(
        userId: String,
        type: TransactionType
    ): LiveData<List<CategoryTotal>> {
        return transactionDao.getTransactionsByCategory(userId, type)
    }

    suspend fun getTransactionById(id: Long): Transactions? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getMaxCategoryId(userId: String): Int? {
        return categoryDao.getMaxCategoryId(userId)
    }


    data class CategoryTotal(
        val categoryName: String,
        val total: Double
    )

}
