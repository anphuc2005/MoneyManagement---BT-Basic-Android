package com.example.moneymanagement.data.data_class

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

    fun groupCategoryByType(
        transactions: List<TransactionWithCategory>,
        type: TransactionType
    ): List<CategoryListItem> {
        return transactions
            .filter { it.transaction.type == type }
            .let { groupCategory(it) }
    }

    fun calculateTotalIncome(categoryItems: List<CategoryListItem>): Double {
        return categoryItems
            .filter { it.category.type == TransactionType.INCOME }
            .sumOf { it.totalAmount }
    }

    fun calculateTotalExpense(categoryItems: List<CategoryListItem>): Double {
        return categoryItems
            .filter { it.category.type == TransactionType.EXPENSE }
            .sumOf { it.totalAmount }
    }


    fun getTopCategories(
        transactions: List<TransactionWithCategory>,
        limit: Int = 5
    ): List<CategoryListItem> {
        return groupCategory(transactions).take(limit)
    }

    fun calculatePercentage(categoryItem: CategoryListItem, total: Double): Double {
        return if (total > 0) (categoryItem.totalAmount / total) * 100 else 0.0
    }
}