package com.example.moneymanagement.data.data_class

import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.data.model.TransactionWithCategory

data class CategoryListItem(
    val category: Category,
    val totalAmount: Double,
    val transactionCount: Int,
    val transactions: List<TransactionWithCategory>
)