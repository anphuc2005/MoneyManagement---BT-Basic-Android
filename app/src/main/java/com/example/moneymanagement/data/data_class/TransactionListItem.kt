package com.example.moneymanagement.data.data_class

import com.example.moneymanagement.data.model.TransactionWithCategory

sealed class TransactionListItem {
    data class DateHeader(val date: String, val dayOfWeek: String) : TransactionListItem()

    data class TransactionItem(val transactionWithCategory: TransactionWithCategory) : TransactionListItem()
}