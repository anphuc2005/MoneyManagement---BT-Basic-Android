package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moneymanagement.R
import com.example.moneymanagement.data.data_class.TransactionListItem
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onItemClick: (TransactionWithCategory) -> Unit,
    private val onItemLongClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(TransactionDiffCallback()) {

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.DateHeader -> TYPE_DATE_HEADER
            is TransactionListItem.TransactionItem -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            TYPE_TRANSACTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> {
                val item = getItem(position) as TransactionListItem.DateHeader
                holder.bind(item.date)
            }
            is TransactionViewHolder -> {
                val item = getItem(position) as TransactionListItem.TransactionItem
                holder.bind(item.transactionWithCategory, onItemClick, onItemLongClick)
            }
        }
    }

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.date_text)
        private val dayTypeText: TextView = itemView.findViewById(R.id.day_type_text)

        fun bind(date: String) {
            dateText.text = date

            // Convert date to day of week
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dayFormat = SimpleDateFormat("EEEE", Locale("vi", "VN"))

                val parsedDate = inputFormat.parse(date)
                parsedDate?.let {
                    dateText.text = outputFormat.format(it)
                    dayTypeText.text = dayFormat.format(it).replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                    }
                }
            } catch (e: Exception) {
                dateText.text = date
                dayTypeText.text = ""
            }
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transactionIcon: ImageView = itemView.findViewById(R.id.transaction_icon)
        private val transactionEmoji: TextView = itemView.findViewById(R.id.transaction_emoji)
        private val transactionTitle: TextView = itemView.findViewById(R.id.transaction_title)
        private val transactionCategory: TextView = itemView.findViewById(R.id.transaction_category)
        private val transactionAmount: TextView = itemView.findViewById(R.id.transaction_amount)
        private val walletType: TextView = itemView.findViewById(R.id.wallet_type)

        fun bind(
            transaction: TransactionWithCategory,
            onItemClick: (TransactionWithCategory) -> Unit,
            onItemLongClick: (TransactionWithCategory) -> Unit
        ) {
            transactionTitle.text = transaction.transaction.transaction_name

            transactionCategory.text = transaction.category?.type_name ?: "Không xác định"

            val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            val formattedAmount = formatter.format(transaction.transaction.amount)

            when (transaction.transaction.type) {
                TransactionType.INCOME -> {
                    transactionAmount.text = "+${formattedAmount} đ"
                    transactionAmount.setTextColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                TransactionType.EXPENSE -> {
                    transactionAmount.text = "-${formattedAmount} đ"
                    transactionAmount.setTextColor(itemView.context.getColor(R.color.red))
                }
            }

            walletType.text = "Ví của tôi"

            setIconForCategory(transaction.category?.icon)

            itemView.setOnClickListener {
                onItemClick(transaction)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(transaction)
                true
            }
        }

        private fun setIconForCategory(iconName: String?) {
            transactionEmoji.visibility = View.GONE
            transactionIcon.visibility = View.GONE

            if (iconName.isNullOrEmpty()) {
                transactionEmoji.visibility = View.VISIBLE
                transactionEmoji.text = "💰"
                return
            }

            when {
                iconName.startsWith("/") -> {
                    val file = File(iconName)
                    if (file.exists()) {
                        transactionIcon.visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(file)
                            .centerCrop()
                            .placeholder(R.drawable.salary)
                            .error(R.drawable.salary)
                            .into(transactionIcon)
                    } else {
                        transactionEmoji.visibility = View.VISIBLE
                        transactionEmoji.text = "📁"
                    }
                }

                iconName.length <= 4 && iconName.any { it.code > 127 } -> {
                    transactionEmoji.visibility = View.VISIBLE
                    transactionEmoji.text = iconName
                }

                else -> {
                    val iconRes = itemView.context.resources.getIdentifier(
                        iconName,
                        "drawable",
                        itemView.context.packageName
                    )
                    if (iconRes != 0) {
                        transactionIcon.visibility = View.VISIBLE
                        transactionIcon.setImageResource(iconRes)
                    } else {
                        transactionEmoji.visibility = View.VISIBLE
                        transactionEmoji.text = "💰"
                    }
                }
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionListItem>() {
        override fun areItemsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
            return when {
                oldItem is TransactionListItem.DateHeader && newItem is TransactionListItem.DateHeader ->
                    oldItem.date == newItem.date
                oldItem is TransactionListItem.TransactionItem && newItem is TransactionListItem.TransactionItem ->
                    oldItem.transactionWithCategory.transaction.id == newItem.transactionWithCategory.transaction.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
            return oldItem == newItem
        }
    }
}