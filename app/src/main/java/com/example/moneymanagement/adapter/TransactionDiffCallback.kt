package com.example.moneymanagement.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.moneymanagement.data.model.TransactionWithCategory

class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionWithCategory>() {
    override fun areItemsTheSame(
        oldItem: TransactionWithCategory,
        newItem: TransactionWithCategory
    ): Boolean {
        return oldItem.transaction.id == newItem.transaction.id
    }

    override fun areContentsTheSame(
        oldItem: TransactionWithCategory,
        newItem: TransactionWithCategory
    ): Boolean {
        return oldItem == newItem
    }
}
