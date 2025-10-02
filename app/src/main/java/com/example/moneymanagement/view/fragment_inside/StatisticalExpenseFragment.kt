package com.example.moneymanagement.view.fragment_inside

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.moneymanagement.R
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentStatisticalExpenseBinding
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class StatisticalExpenseFragment : Fragment() {
    private var _binding: FragmentStatisticalExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticalExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        loadExpenseData()
        setupSpinners()
    }

    private fun setupViewModel() {
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao(), database.categoryDao())
        val factory = TransactionViewModelFactory(repository)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
    }

    private fun loadExpenseData() {
        lifecycleScope.launch {
            val totalExpense = transactionViewModel.getTotalExpense()
            binding.tvAmount.text = formatCurrency(totalExpense)

            loadBarChartData()
            loadPieChartData()
        }
    }

    private fun loadBarChartData() {
        transactionViewModel.getTransactionByType(TransactionType.EXPENSE)
            .observe(viewLifecycleOwner) { transactions ->
                if (transactions.isNullOrEmpty()) {
                    Log.d("StatisticalExpense", "Transactions are null or empty")
                    setupBarChartWithSampleData()
                } else {
                    val groupedByDate = transactions.groupBy { it.transaction.date.substring(0, 10) }
                    val sortedDate = groupedByDate.keys.sorted().takeLast(20)

                    val entries = ArrayList<BarEntry>()
                    sortedDate.forEachIndexed { index, date ->
                        val total = groupedByDate[date]?.sumOf { it.transaction.amount } ?: 0.0
                        entries.add(BarEntry(index.toFloat(), total.toFloat()))
                    }
                    setupBarChart(entries, sortedDate)
                }
            }
    }

    private fun loadPieChartData() {
        transactionViewModel.getTransactionsByCategory(TransactionType.EXPENSE)
            .observe(viewLifecycleOwner) { categoryData ->
                if (categoryData.isNullOrEmpty()) {
                    setupPieChartWithSampleData()
                } else {
                    val entries = ArrayList<PieEntry>()
                    val total = categoryData.sumOf { it.total }

                    categoryData.forEach { data ->
                        val percentage = (data.total / total * 100).toFloat()
                        entries.add(PieEntry(percentage, data.categoryName))
                    }

                    setupPieChart(entries)
                }
            }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(amount)
    }

    private fun setupSpinners() {
        val categories = arrayOf("Theo thể loại", "Theo người", "Theo địa điểm")
        val categoryAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        val months = arrayOf("Monthly", "Daily", "Weekly")
        val years = arrayOf("Year", "2024", "2023")

        val monthAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val yearAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerBarMonth.adapter = monthAdapter
        binding.spinnerBarYear.adapter = yearAdapter
        binding.spinnerPieMonth.adapter = monthAdapter
        binding.spinnerPieYear.adapter = yearAdapter
    }

    private fun setupBarChart(entries: ArrayList<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Chi tiêu")
        dataSet.color = Color.RED
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 0f

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        val barChart = binding.barChart
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(false)
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.GRAY
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.labelCount = minOf(labels.size, 7)

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.DKGRAY
        leftAxis.textColor = Color.GRAY
        leftAxis.axisMinimum = 0f
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when {
                    value >= 1_000_000 -> "${(value / 1_000_000).toInt()}M"
                    value >= 1_000 -> "${(value / 1_000).toInt()}k"
                    else -> value.toInt().toString()
                }
            }
        }

        barChart.axisRight.isEnabled = false
        barChart.invalidate()
    }

    private fun setupBarChartWithSampleData() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 2000000f))
        entries.add(BarEntry(1f, 3500000f))
        entries.add(BarEntry(2f, 1500000f))
        entries.add(BarEntry(3f, 4000000f))
        entries.add(BarEntry(4f, 2500000f))

        val labels = listOf("1Jan", "2Jan", "3Jan", "4Jan", "5Jan")
        setupBarChart(entries, labels)
    }

    private fun setupPieChart(entries: ArrayList<PieEntry>) {
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.RED,
            ContextCompat.getColor(requireContext(), R.color.orange),
            ContextCompat.getColor(requireContext(), R.color.purple),
            ContextCompat.getColor(requireContext(), R.color.pink)
        )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }

        val pieData = PieData(dataSet)

        val pieChart = binding.pieChart
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.legend.textColor = Color.WHITE
        pieChart.setDrawEntryLabels(false)
        pieChart.holeRadius = 70f
        pieChart.transparentCircleRadius = 75f
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setDrawCenterText(false)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = false

        pieChart.invalidate()
    }

    private fun setupPieChartWithSampleData() {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(30f, "Ăn uống"))
        entries.add(PieEntry(25f, "Đi lại"))
        entries.add(PieEntry(20f, "Giải trí"))
        entries.add(PieEntry(25f, "Khác"))

        setupPieChart(entries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
