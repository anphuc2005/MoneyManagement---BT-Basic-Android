package com.example.moneymanagement.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.data.data_class.TransactionListItem
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.databinding.ItemTransactionBinding

class TransactionAdapter(
    private val onItemClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionListItem, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindDateHeader(dateHeader: TransactionListItem.DateHeader) {
            binding.apply {
                dateHeaderLayout.visibility = View.VISIBLE
                dateText.text = dateHeader.date
                dayTypeText.text = dateHeader.dayOfWeek
            }
        }

        fun bindTransaction(transactionWithCategory: TransactionWithCategory) {
            val transaction = transactionWithCategory.transaction
            val category = transactionWithCategory.category

            binding.apply {
                // Ẩn date header cho transaction items
                dateHeaderLayout.visibility = View.GONE

                val iconRes = root.context.resources.getIdentifier(
                    category.icon,
                    "drawable",
                    root.context.packageName
                )
                if (iconRes != 0) {
                    transactionIcon.setImageResource(iconRes)
                }

                transactionTitle.text = transaction.transaction_name
                transactionCategory.text = category.type_name

                transactionAmount.text = when (transaction.type) {
                    TransactionType.INCOME -> "+${formatCurrency(transaction.amount)}"
                    TransactionType.EXPENSE -> "-${formatCurrency(transaction.amount)}"
                }

                transactionAmount.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        when (transaction.type) {
                            TransactionType.INCOME -> android.R.color.holo_green_dark
                            TransactionType.EXPENSE -> android.R.color.holo_red_dark
                        }
                    )
                )

                root.setOnClickListener {
                    onItemClick(transactionWithCategory)
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            return String.format("%,.0f đ", amount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        Log.d("TransactionAdapter", "onCreateViewHolder called")
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.DateHeader -> {
                holder.bindDateHeader(item)
            }
            is TransactionListItem.TransactionItem -> {
                holder.bindTransaction(item.transactionWithCategory)
            }
        }
    }
}
