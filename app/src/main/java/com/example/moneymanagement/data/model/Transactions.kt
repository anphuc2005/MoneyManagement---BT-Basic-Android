package com.example.moneymanagement.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["category_id"])]
)
data class Transactions(
    @PrimaryKey (autoGenerate = true)
    val id: Long = 0,
    val user_id: Int,
    val category_id: Int,
    val transaction_name: String,
    val amount: Double,
//    val date: Date,
    val note: String,
    val type: TransactionType,
    val createdAt: Long = System.currentTimeMillis()
)
