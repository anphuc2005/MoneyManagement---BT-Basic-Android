package com.example.moneymanagement.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("categories")
data class Category(
    @PrimaryKey val id: Int,
    val type_name: String,
    val icon: String,
    val type: TransactionType,
    val userId: String = ""
)
