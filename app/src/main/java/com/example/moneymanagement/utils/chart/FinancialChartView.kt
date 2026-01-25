package com.example.moneymanagement.utils.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.flagging.Flags
import com.example.moneymanagement.R
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import kotlin.math.pow


class FinancialChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var currentMonthlyData: List<MonthlyData> = listOf()
    private var monthLabels = listOf<String>()
    private var maxAmount = 1000000f

    //Bg của chart
    private val bgPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.bg_chart)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    //Style của khung chart (trục x, trục y)
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#37474F")
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    //Style cho text
    private val labelPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.chart_text)
        textSize = 28f
        isAntiAlias = true
    }

    //Style của income line
    private val incomePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.income_line)
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    //Style của expense line
    private val expensePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.expense_line)
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    //Style của Dots (Dữ liệu)
    private val legendDotPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    //Style của Text (Dữ liệu)
    private val legendTextPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        textSize = 32f
        isAntiAlias = true
    }

    fun setupChart(transactions: List<TransactionWithCategory>) {
        val monthlyData = processTransactionByMonth(transactions)
        if(monthlyData.isEmpty()) return
        val highestIncome = monthlyData.maxOfOrNull { it.income } ?: 0.0
        val highestExpense = monthlyData.maxOfOrNull { it.expense } ?: 0.0

        maxAmount = maxOf(highestIncome, highestExpense, 1000000.0).toFloat()
        maxAmount = roundToNiceNumber(maxAmount)

        monthLabels = monthlyData.map { "Tháng ${it.month}"}
        this.currentMonthlyData = monthlyData

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(currentMonthlyData.isEmpty()) return

        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            24f, 24f, bgPaint
        )

        val paddingLeft = 100f
        val paddingRight = 40f
        val paddingTop = 80f
        val paddingBottom = 80f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        drawLegend(canvas, paddingLeft, 30f)

        drawGridAndYAxis(canvas, paddingLeft, paddingTop, chartWidth, chartHeight)




    }

    private fun drawLegend(canvas: Canvas, startX: Float, startY: Float) {
        val dotSize = 10f
        val spacing = 120f

        legendDotPaint.color = ContextCompat.getColor(context, R.color.income_line)
        canvas.drawRect(startX,startY,startX + dotSize, startY + dotSize, legendDotPaint)
        canvas.drawText("Thu Nhập", startX + dotSize + 25f, startY + 10f, legendTextPaint)

        legendDotPaint.color = ContextCompat.getColor(context, R.color.expense_line)
        canvas.drawRect(startX + spacing, startY, startX + spacing + dotSize, startY + dotSize, legendDotPaint)
        canvas.drawText("Chi Tiêu", startX + spacing + dotSize + 25f, startY + 10f, legendTextPaint)
    }

    private fun drawGridAndYAxis(canvas: Canvas, paddingLeft: Float, paddingTop: Float, chartWidth: Float, chartHeight: Float) {
        val gridLine = 5
        labelPaint.textAlign = Paint.Align.RIGHT

        for (i in 0..gridLine) {
            val yPos = paddingTop + (chartHeight * i / gridLine)
            val value = maxAmount * (1 - i.toFloat() / gridLine)

            canvas.drawLine(paddingLeft, yPos, paddingLeft + chartWidth, yPos, gridPaint)
            canvas.drawText(formatAmount(value.toDouble()), paddingLeft - 15f, yPos + 8f, labelPaint)
        }
    }

    private fun XAxisLabels(canvas: Canvas, paddingLeft: Float, paddingTop: Float, chartWidth: Float, chartHeight: Float) {
        labelPaint.textAlign = Paint.Align.CENTER
        val xStep = chartWidth / (currentMonthlyData.size - 1)

        currentMonthlyData.forEachIndexed { i, _ ->
            val x = paddingLeft + (i * xStep)
            canvas.drawText(
                monthLabels[i],x,paddingTop + chartHeight + 50f, labelPaint
            )
        }
    }
    private fun processTransactionByMonth( transactions: List<TransactionWithCategory>) : List<MonthlyData> {
        val monthlyMap = mutableMapOf<Int, MonthlyData>()

        transactions.forEach { transactionWithCategory ->
            try{
                val parts = transactionWithCategory.transaction.date.split("/")
                if(parts.size == 3) {
                    val month = parts[1].toIntOrNull() ?: return@forEach
                    val monthData = monthlyMap.getOrPut(month) {
                        MonthlyData(month, 0.0, 0.0)
                    }

                    when(transactionWithCategory.transaction.type) {
                        TransactionType.INCOME ->
                        {
                            monthData.income += transactionWithCategory.transaction.amount
                        }
                        TransactionType.EXPENSE ->
                        {
                            monthData.expense += transactionWithCategory.transaction.amount
                        }
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return monthlyMap.values.sortedBy { it.month }
    }

    private fun roundToNiceNumber(value: Float): Float {
        val magnitude = 10.0.pow(kotlin.math.floor(kotlin.math.log10(value.toDouble()))).toFloat()
        val normalized = value / magnitude

        val niceNormalized = when {
            normalized < 1.5f -> 1.5f
            normalized < 3f -> 3f
            normalized < 7f -> 7f
            else -> 10f
        }

        return niceNormalized * magnitude
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