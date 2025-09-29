package com.example.moneymanagement.adapter

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.moneymanagement.data.data_class.TransactionListItem
import com.example.moneymanagement.data.model.TransactionWithCategory

class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionListItem>() {
    override fun areItemsTheSame(
        oldItem: TransactionListItem,
        newItem: TransactionListItem
    ): Boolean {
        return when {
            oldItem is TransactionListItem.DateHeader && newItem is TransactionListItem.DateHeader -> {
                oldItem.date == newItem.date
            }
            oldItem is TransactionListItem.TransactionItem && newItem is TransactionListItem.TransactionItem -> {
                oldItem.transactionWithCategory.transaction.id == newItem.transactionWithCategory.transaction.id
            }
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: TransactionListItem,
        newItem: TransactionListItem
    ): Boolean {
        return oldItem == newItem
    }
}
