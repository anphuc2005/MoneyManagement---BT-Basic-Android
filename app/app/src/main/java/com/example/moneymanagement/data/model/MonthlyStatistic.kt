package com.example.moneymanagement.data.model

data class MonthlyStatistic(
    val month: String,
    val type: TransactionType,
    val total: Double
)
