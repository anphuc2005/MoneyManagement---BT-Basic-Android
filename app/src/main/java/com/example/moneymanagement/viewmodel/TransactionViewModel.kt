package com.example.moneymanagement.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanagement.data.model.TransactionSummary
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.Transactions
import com.example.moneymanagement.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransaction = repository.getAllTransactions()
    val allCategories = repository.getAllCategories()

    fun getTransactionByType(type: TransactionType) = repository.getTransactionsByType(type)
    fun getCategoriesByType(type: TransactionType) = repository.getCategoriesByType(type)

    fun insertTransaction(
        transaction_name: String,
        amount: Double,
        category_id: Int,
        date: String,
        note: String = ""
    ) {
        viewModelScope.launch {
            val category = repository.getCategoryById(category_id)
            Log.d("TransactionVM", "Category found: $category")
            if(category != null)
            {
                val transaction = Transactions(
                    transaction_name = transaction_name,
                    amount = amount,
                    category_id = category_id,
                    date = date,
                    note = note,
                    type = category.type,
                    user_id = 0
                )
                Log.d("TransactionVM", "Transaction inserted: $transaction")
                repository.insertTransaction(transaction)
            }
            else {
                Log.e("TransactionVM", "Category not found with id: $category_id")
            }
        }
    }

    fun deleteTransaction(transaction: Transactions) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction
            )
        }
    }

    suspend fun getTransactionSummary(): TransactionSummary {
        return TransactionSummary(
            totalIncome = repository.getTotalIncome(),
            totalExpense = repository.getTotalExpense(),
            balance = repository.getBalance()
        )
    }

}