package com.example.moneymanagement.utils.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.moneymanagement.R
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import kotlin.math.pow

class FinancialChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var currentMonthlyData: List<MonthlyData> = emptyList()
    private var monthLabels: List<String> = emptyList()
    private var maxAmount = 1_000_000f

    // màu background
    private val bgPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.bg_chart)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Trục
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#37474F")
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Chữ
    private val labelPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.chart_text)
        textSize = 28f
        isAntiAlias = true
    }

    // Màu income line
    private val incomePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.income_line)
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    // màu expense line
    private val expensePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.expense_line)
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    // chú thích
    private val legendDotPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val legendTextPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        textSize = 32f
        isAntiAlias = true
    }

    fun setupChart(transactions: List<TransactionWithCategory>) {
        val monthlyData = processTransactionByMonth(transactions)
        if (monthlyData.isEmpty()) return

        val highestIncome = monthlyData.maxOf { it.income }
        val highestExpense = monthlyData.maxOf { it.expense }

        maxAmount = maxOf(highestIncome, highestExpense, 1_000_000.0).toFloat()
        maxAmount = roundToNiceNumber(maxAmount)

        monthLabels = monthlyData.map { "Tháng ${it.month}" }
        currentMonthlyData = monthlyData

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentMonthlyData.isEmpty()) return

        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            24f,
            24f,
            bgPaint
        )

        val paddingLeft = 100f
        val paddingRight = 40f
        val paddingTop = 80f
        val paddingBottom = 80f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        drawLegend(canvas, paddingLeft, 30f)
        drawGridAndYAxis(canvas, paddingLeft, paddingTop, chartWidth, chartHeight)
        drawXAxisLabels(canvas, paddingLeft, paddingTop, chartWidth, chartHeight)
        drawLines(canvas, paddingTop, paddingLeft, chartWidth, chartHeight)
    }

    private fun drawLegend(canvas: Canvas, startX: Float, startY: Float) {
        val dotSize = 20f
        val spacing = 200f

        legendDotPaint.color = ContextCompat.getColor(context, R.color.income_line)
        canvas.drawRect(startX, startY, startX + dotSize, startY + dotSize, legendDotPaint)
        canvas.drawText(
            "Thu Nhập",
            startX + dotSize + 25f,
            startY + 20f,
            legendTextPaint
        )

        legendDotPaint.color = ContextCompat.getColor(context, R.color.expense_line)
        canvas.drawRect(
            startX + spacing,
            startY,
            startX + spacing + dotSize,
            startY + dotSize,
            legendDotPaint
        )
        canvas.drawText(
            "Chi Tiêu",
            startX + spacing + dotSize + 25f,
            startY + 20f,
            legendTextPaint
        )
    }

    private fun drawGridAndYAxis(
        canvas: Canvas,
        paddingLeft: Float,
        paddingTop: Float,
        chartWidth: Float,
        chartHeight: Float
    ) {
        val gridLine = 5
        labelPaint.textAlign = Paint.Align.RIGHT

        for (i in 0..gridLine) {
            val y = paddingTop + chartHeight * i / gridLine
            val value = maxAmount * (1 - i.toFloat() / gridLine)

            canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, gridPaint)
            canvas.drawText(
                formatAmount(value.toDouble()),
                paddingLeft - 15f,
                y + 8f,
                labelPaint
            )
        }
    }

    private fun drawXAxisLabels(
        canvas: Canvas,
        paddingLeft: Float,
        paddingTop: Float,
        chartWidth: Float,
        chartHeight: Float
    ) {
        labelPaint.textAlign = Paint.Align.CENTER
        val xStep = chartWidth / (currentMonthlyData.size - 1)

        currentMonthlyData.forEachIndexed { index, _ ->
            val x = paddingLeft + index * xStep
            canvas.drawText(
                monthLabels[index],
                x,
                paddingTop + chartHeight + 50f,
                labelPaint
            )
        }
    }

    private fun drawLines(
        canvas: Canvas,
        paddingTop: Float,
        paddingLeft: Float,
        chartWidth: Float,
        chartHeight: Float
    ) {
        if (currentMonthlyData.size < 2) return

        val xStep = chartWidth / (currentMonthlyData.size - 1)
        val incomePoints = mutableListOf<Pair<Float, Float>>()
        val expensePoints = mutableListOf<Pair<Float, Float>>()

        currentMonthlyData.forEachIndexed { index, data ->
            val x = paddingLeft + index * xStep

            val yIncome =
                paddingTop + chartHeight -
                        (data.income.toFloat() / maxAmount * chartHeight)

            val yExpense =
                paddingTop + chartHeight -
                        (data.expense.toFloat() / maxAmount * chartHeight)

            incomePoints.add(x to yIncome)
            expensePoints.add(x to yExpense)
        }

        canvas.drawPath(createPath(incomePoints), incomePaint)
        canvas.drawPath(createPath(expensePoints), expensePaint)
    }
}

private fun createPath(points: List<Pair<Float, Float>>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    for (i in 1 until points.size) {
        val (prevX, prevY) = points[i - 1]
        val (currX, currY) = points[i]

        path.moveTo(prevX, prevY)
        path.lineTo(currX, currY)
    }
    return path
}

private fun processTransactionByMonth(
    transactions: List<TransactionWithCategory>
): List<MonthlyData> {

    val monthlyMap = mutableMapOf<Int, MonthlyData>()

    transactions.forEach { item ->
        val parts = item.transaction.date.split("/")
        if (parts.size != 3) return@forEach

        val month = parts[1].toIntOrNull() ?: return@forEach
        val monthData = monthlyMap.getOrPut(month) {
            MonthlyData(month, 0.0, 0.0)
        }

        when (item.transaction.type) {
            TransactionType.INCOME ->
                monthData.income += item.transaction.amount

            TransactionType.EXPENSE ->
                monthData.expense += item.transaction.amount
        }
    }

    return monthlyMap.values.sortedBy { it.month }
}

private fun roundToNiceNumber(value: Float): Float {
    val magnitude =
        10.0.pow(kotlin.math.floor(kotlin.math.log10(value.toDouble()))).toFloat()
    val normalized = value / magnitude

    val niceNormalized = when {
        normalized < 1.5f -> 1.5f
        normalized < 3f -> 3f
        normalized < 7f -> 7f
        else -> 10f
    }

    return niceNormalized * magnitude
}

private fun formatAmount(amount: Double): String =
    when {
        amount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}M"
        amount >= 1_000 -> "${(amount / 1_000).toInt()}K"
        else -> amount.toInt().toString()
    }

data class MonthlyData(
    val month: Int,
    var income: Double,
    var expense: Double
)
