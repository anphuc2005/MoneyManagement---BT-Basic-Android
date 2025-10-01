package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.databinding.ItemCategoryBinding
import com.example.moneymanagement.databinding.ItemTransactionBinding

class CategorySelectionAdapter(
    var onCategorySelected: (Category) -> Unit
) : ListAdapter<Category, CategorySelectionAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                transactionTitle.text = category.type_name

                val iconRes = root.context.resources.getIdentifier(
                    category.icon,
                    "drawable",
                    root.context.packageName
                )
                if (iconRes != 0) {
                    transactionIcon.setImageResource(iconRes)
                }

                root.setOnClickListener {
                    onCategorySelected(category)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

