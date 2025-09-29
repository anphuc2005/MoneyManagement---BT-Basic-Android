package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.databinding.ItemTransactionBinding

class TransactionAdapter(
    private val onItemClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionWithCategory, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    inner class TransactionViewHolder(private val binding : ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(transactionWithCategory: TransactionWithCategory) {
            val transaction = transactionWithCategory.transaction
            val category = transactionWithCategory.category

            binding.apply {
//                val iconRes = getIconResource(root.context, category.icon)
//                if (iconRes != 0) {
//                    binding.transactionIcon.setImageResource(iconRes)
//                }


                binding.transactionTitle.text = transaction.transaction_name
                binding.transactionCategory.text = category.type_name


                binding.transactionAmount.text = when (transaction.type) {
                    TransactionType.INCOME -> "+${transaction.amount}"
                    TransactionType.EXPENSE -> "-${transaction.amount}"
                }

                binding.transactionAmount.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        when (transaction.type) {
                            TransactionType.INCOME -> android.R.color.holo_green_dark
                            TransactionType.EXPENSE -> android.R.color.holo_red_dark
                        }
                    )
                )

//                binding.dateText.text = DateUtil.formatDateForDisplay(transaction.date)
//
//
//                if (transaction.note.isNotEmpty()) {
//                    tvNote.text = transaction.note
//                    tvNote.visibility = View.VISIBLE
//                } else {
//                    tvNote.visibility = View.GONE
//                }

                // Click listener
                root.setOnClickListener {
                    onItemClick(transactionWithCategory)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}