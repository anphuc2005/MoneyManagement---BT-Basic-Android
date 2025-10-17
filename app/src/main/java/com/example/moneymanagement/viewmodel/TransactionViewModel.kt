package com.example.moneymanagement.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.data.model.TransactionSummary
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.data.model.Transactions
import com.example.moneymanagement.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _currentUserId = MutableLiveData<String>()

    fun setUserId(userId: String) {
        if(_currentUserId.value != userId){
            _currentUserId.value = userId
        }
    }

    val allTransaction: LiveData<List<TransactionWithCategory>> = _currentUserId.switchMap { userId ->
        repository.getAllTransactions(userId)
    }

    fun getTransactionByType(type: TransactionType): LiveData<List<TransactionWithCategory>>? {
        return _currentUserId.value?.let { userId ->
            repository.getTransactionsByType(userId, type)
        }
    }


    val totalIncome: LiveData<Double> = allTransaction.map { transactions ->
        transactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { it.transaction.amount }
    }

    val totalExpense: LiveData<Double> = allTransaction.map { transactions ->
        transactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }
    }

    val balance: LiveData<Double> = allTransaction.map { transactions ->
        val income = transactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { it.transaction.amount }
        val expense = transactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }
        income - expense
    }

    fun insertTransaction(
        userId: String,
        transaction_name: String,
        amount: Double,
        category_id: Int,
        date: String,
        note: String = ""
    ) {
        viewModelScope.launch {
            val category = repository.getCategoryById(category_id)
            Log.d("TransactionVM", "Category found: $category")
            if(category != null) {
                val transaction = Transactions(
                    transaction_name = transaction_name,
                    amount = amount,
                    category_id = category_id,
                    date = date,
                    note = note,
                    type = category.type,
                    user_id = userId
                )
                Log.d("TransactionVM", "Transaction inserted: $transaction")
                repository.insertTransaction(transaction)
            } else {
                Log.e("TransactionVM", "Category not found with id: $category_id")
            }
        }
    }


    suspend fun getTransactionSummary(): TransactionSummary {
        return _currentUserId.value?.let { userId ->
            TransactionSummary(
                totalIncome = repository.getTotalIncome(userId),
                totalExpense = repository.getTotalExpense(userId),
                balance = repository.getBalance(userId)
            )
        } ?: TransactionSummary(0.0, 0.0, 0.0)
    }

    val allCategories = repository.getAllCategories()

    fun insertCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.insertCategory(category)
                Log.d("TransactionVM", "Category inserted: $category")
            } catch (e: Exception) {
                Log.e("TransactionVM", "Error inserting category: ${e.message}")
            }
        }
    }


    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                Log.d("TransactionVM", "Category deleted: $category")
            } catch (e: Exception) {
                Log.e("TransactionVM", "Error deleting category: ${e.message}")
            }
        }
    }

    suspend fun getTransactionById(id: Long): Transactions? {
        return repository.getTransactionById(id)
    }

    suspend fun getCategoryById(id: Int): Category? {
        return repository.getCategoryById(id)
    }

    suspend fun updateTransaction(transaction: Transactions) {
        repository.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transactions) {
        repository.deleteTransaction(transaction)
    }

    suspend fun getNextCategoryId(): Int {
        val userId = _currentUserId.value
            ?: com.example.moneymanagement.data.data_class.UserManager.getCurrentUserId()
            ?: return 1

        val maxId = repository.getMaxCategoryId(userId) ?: 0
        return maxId + 1
    }

}