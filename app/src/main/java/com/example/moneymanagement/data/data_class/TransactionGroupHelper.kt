package com.example.moneymanagement.data.data_class
import com.example.moneymanagement.data.model.TransactionWithCategory
import java.text.SimpleDateFormat
import java.util.*

object TransactionGroupHelper {

    fun groupTransactionsByDate(
        transactions: List<TransactionWithCategory>
    ): List<TransactionListItem> {
        val result = mutableListOf<TransactionListItem>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        val dayFormat = SimpleDateFormat("EEEE", Locale("vi", "VN"))

        val grouped = transactions.groupBy { transaction ->
            transaction.transaction.date
        }

        val sortedGroups = grouped.toSortedMap(compareByDescending { dateString ->
            try {
                dateFormat.parse(dateString)?.time ?: 0
            } catch (e: Exception) {
                0L
            }
        })

        sortedGroups.forEach { (dateString, transactionsForDate) ->
            val dayOfWeek = try {
                val date = dateFormat.parse(dateString)
                if (date != null) {
                    dayFormat.format(date).replaceFirstChar { it.uppercase() }
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }

            result.add(
                TransactionListItem.DateHeader(
                    date = dateString,
                    dayOfWeek = dayOfWeek
                )
            )

            transactionsForDate
                .sortedByDescending { it.transaction.date }
                .forEach { transaction ->
                    result.add(
                        TransactionListItem.TransactionItem(transaction)
                    )
                }
        }

        return result
    }
}