package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moneymanagement.R
import com.example.moneymanagement.data.data_class.CategoryListItem
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.databinding.ItemTransactionBinding
import java.io.File
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

                when {
                    item.category.icon.startsWith("/") -> {
                        val file = File(item.category.icon)
                        if(file.exists()) {
                            transactionIcon.visibility = View.VISIBLE
                            transactionEmoji.visibility = View.GONE

                            Glide.with(root.context)
                                .load(file)
                                .centerCrop()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .into(transactionIcon)
                        }
                        else {
                            transactionIcon.visibility = View.GONE
                            transactionEmoji.visibility = View.VISIBLE
                            transactionEmoji.text = "📁"
                        }
                    }

                    item.category.icon.length <= 4 && item.category.icon.any { it.code > 127 } -> {
                        transactionIcon.visibility = View.GONE
                        transactionEmoji.visibility = View.VISIBLE
                        transactionEmoji.text = item.category.icon
                    }

                    else -> {
                        val iconRes = root.context.resources.getIdentifier(
                            item.category.icon,
                            "drawable",
                            root.context.packageName
                        )
                        if (iconRes != 0) {
                            transactionIcon.visibility = View.VISIBLE
                            transactionEmoji.visibility = View.GONE
                            transactionIcon.setImageResource(iconRes)
                        } else {
                            transactionIcon.visibility = View.GONE
                            transactionEmoji.visibility = View.VISIBLE
                            transactionEmoji.text = "📁"
                        }
                    }
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