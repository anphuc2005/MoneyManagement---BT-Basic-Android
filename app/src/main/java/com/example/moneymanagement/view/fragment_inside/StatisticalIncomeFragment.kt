package com.example.moneymanagement.view.fragment_inside

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.StatisticalChoiceAdapter
import com.example.moneymanagement.data.data_class.UserManager
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentStatisticalIncomeBinding
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticalIncomeFragment : Fragment() {
    private var _binding: FragmentStatisticalIncomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel

    private var selectedCategory = "Theo thể loại"
    private var selectedBarMonth = "Monthly"
    private var selectedBarYear = "Year"
    private var selectedPieMonth = "Monthly"
    private var selectedPieYear = "Year"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticalIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupViewModel() {
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao(), database.categoryDao())
        val factory = TransactionViewModelFactory(repository)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        UserManager.getCurrentUserId()?.let { userId ->
            transactionViewModel.setUserId(userId)
            Log.d("StatisticalIncome", "Set userId: $userId")
        }
    }

    private fun observeViewModel() {
        transactionViewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvAmount.text = formatCurrency(income ?: 0.0)
        }

        transactionViewModel.getTransactionByType(TransactionType.INCOME)
            ?.observe(viewLifecycleOwner) { transactions ->
                if (transactions.isNullOrEmpty()) {
                    Log.d("StatisticalIncome", "Transactions are null or empty")
                    setupBarChartWithSampleData()
                    setupPieChartWithSampleData()
                } else {
                    loadBarChartData()
                    loadPieChartData()
                }
            }
    }

    private fun setupClickListeners() {
        binding.spinnerBarMonth.setOnClickListener {
            showMonthBottomSheet(true)
        }

        binding.spinnerBarYear.setOnClickListener {
            showYearBottomSheet(true)
        }

        binding.spinnerPieMonth.setOnClickListener {
            showMonthBottomSheet(false)
        }

        binding.spinnerPieYear.setOnClickListener {
            showYearBottomSheet(false)
        }
    }

    private fun showMonthBottomSheet(isForBarChart: Boolean) {
        val months = arrayOf("Monthly", "Daily", "Weekly")

        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_category_selection, null)

        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.rv_categories)
        val btnClose = bottomSheetView.findViewById<ImageView>(R.id.btn_close)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = StatisticalChoiceAdapter(months.toList()) { selectedItem ->
            if (isForBarChart) {
                selectedBarMonth = selectedItem
                binding.spinnerBarMonth.text = selectedItem
            } else {
                selectedPieMonth = selectedItem
                binding.spinnerPieMonth.text = selectedItem
            }
            handleMonthSelected(selectedItem, isForBarChart)
            bottomSheetDialog.dismiss()
        }
        recyclerView.adapter = adapter

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun showYearBottomSheet(isForBarChart: Boolean) {
        val years = arrayOf("Year", "2024", "2023")

        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_category_selection, null)

        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.rv_categories)
        val btnClose = bottomSheetView.findViewById<ImageView>(R.id.btn_close)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = StatisticalChoiceAdapter(years.toList()) { selectedItem ->
            if (isForBarChart) {
                selectedBarYear = selectedItem
                binding.spinnerBarYear.text = selectedItem
            } else {
                selectedPieYear = selectedItem
                binding.spinnerPieYear.text = selectedItem
            }
            handleYearSelected(selectedItem, isForBarChart)
            bottomSheetDialog.dismiss()
        }
        recyclerView.adapter = adapter

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun handleMonthSelected(month: String, isForBarChart: Boolean) {
        Log.d("StatisticalIncome", "Selected month: $month for ${if (isForBarChart) "BarChart" else "PieChart"}")
        if (isForBarChart) {
            loadBarChartData()
        } else {
            loadPieChartData()
        }
    }

    private fun handleYearSelected(year: String, isForBarChart: Boolean) {
        Log.d("StatisticalIncome", "Selected year: $year for ${if (isForBarChart) "BarChart" else "PieChart"}")
        if (isForBarChart) {
            loadBarChartData()
        } else {
            loadPieChartData()
        }
    }

    private fun loadBarChartData() {
        transactionViewModel.getTransactionByType(TransactionType.INCOME)
            ?.observe(viewLifecycleOwner) { transactions ->
                if (transactions.isNullOrEmpty()) {
                    Log.d("StatisticalIncome", "Transactions are null or empty")
                    setupBarChartWithSampleData()
                } else {
                    val filteredTransactions = filterTransactionsByYearAndPeriod(
                        transactions,
                        selectedBarYear,
                        selectedBarMonth
                    )

                    when (selectedBarMonth) {
                        "Daily" -> setupDailyBarChart(filteredTransactions)
                        "Weekly" -> setupWeeklyBarChart(filteredTransactions)
                        "Monthly" -> setupMonthlyBarChart(filteredTransactions)
                    }
                }
            }
    }

    private fun loadPieChartData() {
        transactionViewModel.getTransactionByType(TransactionType.INCOME)
            ?.observe(viewLifecycleOwner) { transactions ->
                if (transactions.isNullOrEmpty()) {
                    setupPieChartWithSampleData()
                } else {
                    val filteredTransactions = filterTransactionsByYearAndPeriod(
                        transactions,
                        selectedPieYear,
                        selectedPieMonth
                    )

                    val categoryMap = filteredTransactions.groupBy { it.category.type_name }
                    val entries = ArrayList<PieEntry>()
                    val total = categoryMap.values.sumOf { list ->
                        list.sumOf { it.transaction.amount }
                    }

                    if (total > 0) {
                        categoryMap.forEach { (categoryName, transactionList) ->
                            val categoryTotal = transactionList.sumOf { it.transaction.amount }
                            val percentage = (categoryTotal / total * 100).toFloat()
                            entries.add(PieEntry(percentage, categoryName))
                        }
                        setupPieChart(entries)
                    } else {
                        setupPieChartWithSampleData()
                    }
                }
            }
    }

    private fun filterTransactionsByYearAndPeriod(
        transactions: List<com.example.moneymanagement.data.model.TransactionWithCategory>,
        year: String,
        period: String
    ): List<com.example.moneymanagement.data.model.TransactionWithCategory> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        return transactions.filter { transactionWithCategory ->
            val transactionDate = transactionWithCategory.transaction.date.substring(0, 10)

            val yearMatches = if (year == "Year") {
                true
            } else {
                val parts = transactionDate.split("/")
                parts.size == 3 && parts[2] == year
            }

            yearMatches
        }
    }

    private fun setupDailyBarChart(transactions: List<com.example.moneymanagement.data.model.TransactionWithCategory>) {
        val groupedByDate = transactions.groupBy { it.transaction.date.substring(0, 10) }
        val sortedDates = groupedByDate.keys.sorted().takeLast(30)

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        sortedDates.forEachIndexed { index, date ->
            val total = groupedByDate[date]?.sumOf { it.transaction.amount } ?: 0.0
            entries.add(BarEntry(index.toFloat(), total.toFloat()))

            val parts = date.split("-")
            if (parts.size == 3) {
                labels.add("${parts[2]}/${parts[1]}")
            } else {
                labels.add(date)
            }
        }

        setupBarChart(entries, labels)
    }

    private fun setupWeeklyBarChart(transactions: List<com.example.moneymanagement.data.model.TransactionWithCategory>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val groupedByWeek = transactions.groupBy { transaction ->
            val date = dateFormat.parse(transaction.transaction.date.substring(0, 10))
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val week = calendar.get(Calendar.WEEK_OF_YEAR)
            "$year-W$week"
        }

        val sortedWeeks = groupedByWeek.keys.sorted().takeLast(20)

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        sortedWeeks.forEachIndexed { index, week ->
            val total = groupedByWeek[week]?.sumOf { it.transaction.amount } ?: 0.0
            entries.add(BarEntry(index.toFloat(), total.toFloat()))

            val weekNumber = week.split("-W").getOrNull(1) ?: week
            labels.add("W$weekNumber")
        }

        setupBarChart(entries, labels)
    }

    private fun setupMonthlyBarChart(transactions: List<com.example.moneymanagement.data.model.TransactionWithCategory>) {
        val groupedByMonth = transactions.groupBy {
            val parts = it.transaction.date.split("/") //dd/mm/yyyy
            if (parts.size == 3) "${parts[1]}/${parts[2]}" else it.transaction.date
        }
        val sortedMonths = groupedByMonth.keys.sortedWith(compareBy {
            val parts = it.split("/")
            if (parts.size == 2) parts[1].toInt() * 100 + parts[0].toInt() else 0
        }).takeLast(12)

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        sortedMonths.forEachIndexed { index, month ->
            val total = groupedByMonth[month]?.sumOf { it.transaction.amount } ?: 0.0
            entries.add(BarEntry(index.toFloat(), total.toFloat()))
            labels.add(month)
        }

        setupBarChart(entries, labels)
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(amount)
    }

    private fun setupBarChart(entries: ArrayList<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Thu nhập")
        dataSet.color = Color.CYAN
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
        entries.add(BarEntry(0f, 3000000f))
        entries.add(BarEntry(1f, 0f))
        entries.add(BarEntry(2f, 5500000f))
        entries.add(BarEntry(3f, 0f))
        entries.add(BarEntry(4f, 6000000f))

        val labels = listOf("1Jan", "2Jan", "3Jan", "4Jan", "5Jan")
        setupBarChart(entries, labels)
    }

    private fun setupPieChart(entries: ArrayList<PieEntry>) {
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.CYAN,
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
        entries.add(PieEntry(40f, "OT"))
        entries.add(PieEntry(30f, "Lãi cho vay"))
        entries.add(PieEntry(20f, "Parttime"))
        entries.add(PieEntry(10f, "Tiền lương chính"))

        setupPieChart(entries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}