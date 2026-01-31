package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moneymanagement.R
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.databinding.ItemCategoryGridBinding
import java.io.File

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

    inner class CategoryViewHolder(private val binding: ItemCategoryGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                tvCategoryName.text = category.type_name

                loadCategoryIcon(category.icon)

                root.setOnClickListener {
                    onCategoryClick(category)
                }

                root.setOnLongClickListener {
                    onCategoryLongClick(category)
                }
            }
        }

        private fun loadCategoryIcon(iconPath: String) {
            binding.tvIcon.apply {
                when {
                    iconPath.length <= 4 && iconPath.matches(Regex("[\uD83C-\uDBFF\uDC00-\uDFFF]+")) -> {
                        setImageDrawable(null)
                        android.util.Log.d("CategoryAdapter", "Loading emoji: $iconPath")
                        setImageResource(android.R.drawable.ic_menu_gallery)
                    }

                    iconPath.startsWith("/") -> {
                        val file = File(iconPath)
                        if (file.exists()) {
                            android.util.Log.d("CategoryAdapter", "Loading file: $iconPath")
                            Glide.with(context)
                                .load(file)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .centerCrop()
                                .into(this)
                        } else {
                            android.util.Log.e("CategoryAdapter", "File not found: $iconPath")
                            setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    }

                    else -> {
                        val iconRes = context.resources.getIdentifier(
                            iconPath,
                            "drawable",
                            context.packageName
                        )
                        if (iconRes != 0) {
                            android.util.Log.d("CategoryAdapter", "Loading resource: $iconPath")
                            setImageResource(iconRes)
                        } else {
                            android.util.Log.w("CategoryAdapter", "Resource not found: $iconPath")
                            setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    }
                }
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