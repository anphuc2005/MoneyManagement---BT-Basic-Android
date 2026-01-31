package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.databinding.ItemCategoryBinding
import com.example.moneymanagement.databinding.StatisticalChoiceBinding

class StatisticalChoiceAdapter(private val categories: List<String>,
                               private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<StatisticalChoiceAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(val binding: StatisticalChoiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StatisticalChoiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.apply {
            tvCategory.text = category
            root.isSelected = position == selectedPosition

            root.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(category)
            }
        }
    }

    override fun getItemCount() = categories.size
}