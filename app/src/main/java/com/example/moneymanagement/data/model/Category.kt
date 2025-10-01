package com.example.moneymanagement.data.model

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity("categories")
data class Category(
    @PrimaryKey val id: Int,
    val type_name: String,
    val icon: String,
    val type: TransactionType
)
