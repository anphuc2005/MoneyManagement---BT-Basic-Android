package com.example.moneymanagement.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.R
import com.example.moneymanagement.data.data_class.TransactionListItem
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.databinding.ItemDateHeaderBinding
import com.example.moneymanagement.databinding.ItemTransactionBinding

class TransactionAdapter(
    private val onItemClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(TransactionDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }

    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dateHeader: TransactionListItem.DateHeader) {
            binding.apply {
                dateText.text = dateHeader.date
                dayTypeText.text = dateHeader.dayOfWeek

                root.isClickable = false
                root.isFocusable = false
            }
        }
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transactionWithCategory: TransactionWithCategory) {
            val transaction = transactionWithCategory.transaction
            val category = transactionWithCategory.category

            binding.apply {
                val iconRes = root.context.resources.getIdentifier(
                    category?.icon ?: "ic_default",
                    "drawable",
                    root.context.packageName
                )
                if (iconRes != 0) {
                    transactionIcon.setImageResource(iconRes)
                } else {
                    transactionIcon.setImageResource(R.drawable.circle)
                }

                transactionTitle.text = transaction.transaction_name
                transactionCategory.text = category?.type_name ?: "Không xác định"

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

                walletType.text = "Ví của tôi"

                root.setOnClickListener {
                    onItemClick(transactionWithCategory)
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            return String.format("%,.0f đ", amount)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.DateHeader -> VIEW_TYPE_DATE_HEADER
            is TransactionListItem.TransactionItem -> VIEW_TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("TransactionAdapter", "onCreateViewHolder called for viewType: $viewType")

        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DateHeaderViewHolder(binding)
            }
            VIEW_TYPE_TRANSACTION -> {
                val binding = ItemTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TransactionViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("TransactionAdapter", "onBindViewHolder at position: $position")
        when (val item = getItem(position)) {
            is TransactionListItem.DateHeader -> {
                (holder as DateHeaderViewHolder).bind(item)
            }
            is TransactionListItem.TransactionItem -> {
                (holder as TransactionViewHolder).bind(item.transactionWithCategory)
            }
        }
    }
}