package com.example.moneymanagement.utils

import com.example.moneymanagement.data.data_class.CategoryListItem
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory

object CategoryGroupHelper {
    fun groupCategory(transactions: List<TransactionWithCategory>): List<CategoryListItem> {
        return transactions.groupBy { it.category.id }
            .map { entry ->
                val firstCategory = entry.value.first().category
                val total = entry.value.sumOf { it.transaction.amount }
                val count = entry.value.size

                CategoryListItem(
                    category = firstCategory,
                    totalAmount = total,
                    transactionCount = count,
                    transactions = entry.value
                )
            }
            .sortedByDescending { it.totalAmount }
    }
}