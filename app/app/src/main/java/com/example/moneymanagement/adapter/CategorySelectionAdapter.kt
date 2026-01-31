package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.databinding.ItemCategoryBinding
import com.example.moneymanagement.databinding.ItemTransactionBinding
import java.io.File

class CategorySelectionAdapter(
    var onCategorySelected: (Category) -> Unit
) : ListAdapter<Category, CategorySelectionAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                transactionTitle.text = category.type_name

                when {
                    category.icon.startsWith("/") -> {
                        val file = File(category.icon)
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

                    category.icon.length <= 4 && category.icon.any { it.code > 127 } -> {
                        transactionIcon.visibility = View.GONE
                        transactionEmoji.visibility = View.VISIBLE
                        transactionEmoji.text = category.icon
                    }

                    else -> {
                        val iconRes = root.context.resources.getIdentifier(
                            category.icon,
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

