package com.example.moneymanagement.view.fragment_inside

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.CategoryGroupAdapter
import com.example.moneymanagement.data.data_class.CategoryGroupHelper
import com.example.moneymanagement.data.data_class.CategoryListItem
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentWalletBinding
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var categoryAdapter: CategoryGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViewModel() {
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao(), database.categoryDao())
        val factory = TransactionViewModelFactory(repository)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryGroupAdapter { categoryItem ->
            showTransactionDetail(categoryItem)
        }

        binding.transactionRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun observeData() {
        transactionViewModel.allTransaction.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isNullOrEmpty()) {
                binding.transactionRecyclerView.visibility = View.GONE
            } else {

                val groupedItems = CategoryGroupHelper.groupCategory(transactions)

                val groupedCategories = CategoryGroupHelper.groupCategory(transactions)

                val totalIncome = CategoryGroupHelper.calculateTotalIncome(groupedCategories)
                binding.tvBalance.text = String.format("%,.0f đ", totalIncome)

                categoryAdapter.submitList(groupedItems) {
                    Log.d("WalletFragment", "Adapter item count: ${categoryAdapter.itemCount}")
                    if (groupedItems.isNotEmpty()) {
                        binding.transactionRecyclerView.scrollToPosition(0)
                    }
                }

                updateChartData(transactions)
            }
        }

        lifecycleScope.launch {
            val summary = transactionViewModel.getTransactionSummary()
        }
    }

    private fun updateChartData(transactions: List<TransactionWithCategory>) {
        val groupCategories = CategoryGroupHelper.groupCategory(transactions)

        val totalIncome = CategoryGroupHelper.calculateTotalIncome(groupCategories)
        val totalExpense = CategoryGroupHelper.calculateTotalExpense(groupCategories)
        val balance = totalIncome - totalExpense

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        binding.tvBalance.text = formatter.format(balance)
        binding.tvIncome.text = "+${formatter.format(totalIncome)}"
        binding.tvExpense.text = "-${formatter.format(totalExpense)}"

        binding.incomeProgress.max = totalIncome.toInt()
        binding.incomeProgress.progress = totalIncome.toInt()

        binding.expenseProgress.max = totalIncome.toInt()
        binding.expenseProgress.progress = totalExpense.toInt()
    }



    private fun showTransactionDetail(categoryItem: CategoryListItem) {
        Log.d("WalletFragment", """
            Category clicked:
            - Name: ${categoryItem.category.type_name}
            - Total: ${categoryItem.totalAmount}
            - Count: ${categoryItem.transactionCount}
            - Type: ${categoryItem.category.type}
        """.trimIndent())

        showCategoryDetailDialog(categoryItem)
    }

    private fun showCategoryDetailDialog(categoryItem: CategoryListItem) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        val message = """
            Danh mục: ${categoryItem.category.type_name}
            Tổng tiền: ${formatter.format(categoryItem.totalAmount)}
            Số giao dịch: ${categoryItem.transactionCount}
            Loại: ${if (categoryItem.category.type == TransactionType.INCOME) "Thu nhập" else "Chi tiêu"}
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Chi tiết danh mục")
            .setMessage(message)
            .setPositiveButton("Xem chi tiết") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Đóng") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        fun newInstance() = WalletFragment()

        private const val TAG = "WalletFragment"
    }
}