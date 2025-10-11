package com.example.moneymanagement.adapter

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.R
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.databinding.ItemCategoryGridBinding

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onCategoryLongClick: (Category) -> Boolean
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryGridBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(category: Category) {
//            tvIcon.text = category.icon
//            tvCategoryName.text = category.type_name
//
//            itemView.setOnClickListener {
//                onCategoryClick(category)
//            }
//
//            itemView.setOnLongClickListener {
//                onCategoryLongClick(category)
//            }
            binding.apply {
                tvCategoryName.text = category.type_name
                val iconRes = root.context.resources.getIdentifier(
                    category.icon,
                    "drawable",
                    root.context.packageName
                )
                if(iconRes != 0) tvIcon.setImageResource(iconRes)
                else tvIcon.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}