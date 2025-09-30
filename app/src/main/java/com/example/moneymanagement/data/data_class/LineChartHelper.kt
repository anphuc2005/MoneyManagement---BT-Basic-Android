package com.example.moneymanagement.data.data_class

import android.graphics.Color
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object LineChartHelper {
    fun setupLineChart(
        lineChart: LineChart,
        transactions: List<TransactionWithCategory>
    ) {
        if (transactions.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            return
        }
        val monthlyData = processTransactionsByMonth(transactions)

        if (monthlyData.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            return
        }

        val incomeEntries = ArrayList<Entry>()
        val expenseEntries = ArrayList<Entry>()
        val monthLabels = ArrayList<String>()

        monthlyData.forEachIndexed { index, data ->
            incomeEntries.add(Entry(index.toFloat(), data.income.toFloat()))
            expenseEntries.add(Entry(index.toFloat(), data.expense.toFloat()))
            monthLabels.add("T${data.month}")
        }


        val incomeDataSet = LineDataSet(incomeEntries, "Thu nhập").apply {
            color = Color.parseColor("#E91E63")
            setCircleColor(Color.parseColor("#E91E63"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            cubicIntensity = 0.2f
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Chi tiêu").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#4CAF50"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            cubicIntensity = 0.2f
        }

        val dataSets = arrayListOf<ILineDataSet>(incomeDataSet, expenseDataSet)
        val lineData = LineData(dataSets)

        configureLineChart(lineChart, lineData, monthLabels)
    }

    private fun configureLineChart(
        lineChart: LineChart,
        lineData: LineData,
        monthLabels: List<String>
    ) {
        lineChart.apply {
            data = lineData
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.parseColor("#2E3A4A"))

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#B0BEC5")
                textSize = 11f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#4A5A6A")
                granularity = 1f
                setLabelCount(monthLabels.size, false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < monthLabels.size) {
                            monthLabels[index]
                        } else {
                            ""
                        }
                    }
                }
            }

            axisLeft.apply {
                textColor = Color.parseColor("#B0BEC5")
                textSize = 11f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#3A4A5A")
                gridLineWidth = 0.5f
                setDrawAxisLine(true)
                axisLineColor = Color.parseColor("#4A5A6A")
                axisMinimum = 0f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatAmount(value.toDouble())
                    }
                }
            }

            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                textColor = Color.parseColor("#B0BEC5")
                textSize = 12f
                form = Legend.LegendForm.CIRCLE
                formSize = 10f
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 20f
                yEntrySpace = 0f
            }

            animateX(1000)
            invalidate()
        }
    }

    private fun processTransactionsByMonth(
        transactions: List<TransactionWithCategory>
    ): List<MonthlyData> {

        val monthlyMap = mutableMapOf<Int, MonthlyData>()

        transactions.forEach { transactionWithCategory ->
            try {
                val parts = transactionWithCategory.transaction.date.split("/")
                if (parts.size == 3) {
                    val month = parts[1].toIntOrNull() ?: return@forEach

                    val monthData = monthlyMap.getOrPut(month) {
                        MonthlyData(month, 0.0, 0.0)
                    }

                    when (transactionWithCategory.transaction.type) {
                        TransactionType.INCOME -> {
                            monthData.income += transactionWithCategory.transaction.amount
                        }
                        TransactionType.EXPENSE -> {
                            monthData.expense += transactionWithCategory.transaction.amount
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return monthlyMap.values.sortedBy { it.month }
    }

    private fun formatAmount(amount: Double): String {
        return when {
            amount >= 1000000 -> "${(amount / 1000000).toInt()}M"
            amount >= 1000 -> "${(amount / 1000).toInt()}K"
            else -> amount.toInt().toString()
        }
    }

    data class MonthlyData(
        val month: Int,
        var income: Double,
        var expense: Double
    )
}