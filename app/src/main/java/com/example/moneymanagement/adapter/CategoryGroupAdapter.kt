package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.R
import com.example.moneymanagement.data.data_class.CategoryListItem
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryGroupAdapter(private val onItemClick: (CategoryListItem) -> Unit) :
    ListAdapter<CategoryListItem, CategoryGroupAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class CategoryViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(item: CategoryListItem, onItemClick: (CategoryListItem) -> Unit) {
            binding.apply {
                transactionTitle.text = item.category.type_name

                val iconRes = root.context.resources.getIdentifier(
                    item.category.icon,
                    "drawable",
                    root.context.packageName
                )
                if (iconRes != 0) {
                    transactionIcon.setImageResource(iconRes)
                } else {
                    transactionIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                transactionAmount.text = when (item.category.type) {
                    TransactionType.INCOME -> "+${formatCurrency(item.totalAmount)}"
                    TransactionType.EXPENSE -> "-${formatCurrency(item.totalAmount)}"
                }

                transactionAmount.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        when (item.category.type) {
                            TransactionType.INCOME -> android.R.color.holo_green_dark
                            TransactionType.EXPENSE -> android.R.color.holo_red_dark
                        }
                    )
                )

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            return String.format("%,.0f đ", amount)
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryListItem>() {
        override fun areItemsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem): Boolean {
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem): Boolean {
            return oldItem == newItem
        }
    }
}