package com.example.moneymanagement.utils.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.moneymanagement.R
import java.text.DecimalFormat
import kotlin.math.max

class ChartWalletView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var incomeAmount: Double = 0.0
    private var expenseAmount: Double = 0.0

    private var totalAmount: Double = 0.0
    private var maxAmount: Double = 1e9+7

    private val barHeight = 24f
    private val barCornerRadius = 12f
    private val verticalSpacing = 80f
    private val horizontalPadding = 40f

    private val bgPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.bg_chart)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val bgProgressBar = Paint().apply {
        color = ContextCompat.getColor(context, R.color.total_line)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val labelTotalPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        textSize = 36f
        isAntiAlias = true
    }

    private val valuePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        textSize = 32f  // ← Tăng lên
        isAntiAlias = true
    }


    private val lineTotalPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.total_line)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val labelIncomePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.income_line)
        textSize = 12f
        isAntiAlias = true
    }

    private val lineIncomePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.income_line)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val labelExpensePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.expense_line)
        textSize = 12f
        isAntiAlias = true
    }

    private val lineExpensePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.expense_line)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setupChart(totalIncome: Double, totalExpense: Double) {
        incomeAmount = totalIncome
        expenseAmount = totalExpense
        totalAmount = totalIncome - totalExpense
        maxAmount = totalIncome + totalExpense

        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)

        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> wSize
            else -> wSize
        }

        val height = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> {
                minOf((width * 0.4).toInt(), hSize)
            }
            else -> (width * 0.4).toInt()
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d("ChartWalletView", "Width: $width, Height: $height")

        val contentWidth = width - (horizontalPadding * 2)
        var currentY = 60f

        canvas.drawRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            24f,
            24f,
            bgPaint
        )

        labelPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Tài khoản của tôi", horizontalPadding, currentY, labelPaint)

        labelTotalPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(formatAmount(totalAmount), width - horizontalPadding, currentY, labelTotalPaint)

        currentY += verticalSpacing

        drawProgressBar(canvas, "Thu Nhập", incomeAmount, lineIncomePaint, currentY, contentWidth, true)

        currentY += verticalSpacing * 2

        drawProgressBar(canvas, "Chi Tiêu", expenseAmount, lineExpensePaint, currentY, contentWidth, false)
    }

    private fun drawProgressBar(
        canvas: Canvas, label: String, value: Double, paint: Paint, startY: Float,
        contentWidth: Float, isPositive: Boolean
    ) {
        labelPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(label, horizontalPadding, startY, labelPaint)

        val barTop = startY + 10f
        val bgRect = RectF(horizontalPadding, barTop,
            (horizontalPadding + contentWidth), barTop + barHeight)
        canvas.drawRoundRect(bgRect, barCornerRadius, barCornerRadius, bgProgressBar)
        val progress = if (maxAmount > 0) {
            (value / maxAmount).coerceIn(0.0,1.0).toFloat()
        } else 0f
        Log.d("BarProgess", "progress = ${progress}")
        val progressWidth = contentWidth * progress
        if(progressWidth > 0) {
            val progressRect = RectF(horizontalPadding, barTop,
                (horizontalPadding + progressWidth), barTop + barHeight)
            canvas.drawRoundRect(progressRect, barCornerRadius, barCornerRadius, paint)
        }

        val sign = if (isPositive) "+" else "-"
        val valueText = "$sign${formatAmount(value)}"

        valuePaint.textAlign = Paint.Align.LEFT
        valuePaint.color = paint.color
        canvas.drawText(valueText, horizontalPadding, barTop + barHeight + 40f, valuePaint)

    }
    private fun formatAmount(amount: Double): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(amount) + " VNĐ"
    }

}