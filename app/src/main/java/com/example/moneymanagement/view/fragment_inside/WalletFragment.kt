package com.example.moneymanagement.view.fragment_inside

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.CategoryGroupAdapter
import com.example.moneymanagement.data.data_class.CategoryGroupHelper
import com.example.moneymanagement.data.data_class.CategoryListItem
import com.example.moneymanagement.data.data_class.UserManager
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

    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        if (!UserManager.isUserLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        currentUserId = UserManager.getCurrentUserId()

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Không thể lấy thông tin user", Toast.LENGTH_SHORT).show()
            return
        }
        setupViewModel()
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
        currentUserId?.let { userId ->
            transactionViewModel.setUserId(userId)
            Log.d("HomeFragment", "Set userId: $userId")
        }
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
                binding.transactionRecyclerView.visibility = View.VISIBLE
                val groupedItems = CategoryGroupHelper.groupCategory(transactions)
                categoryAdapter.submitList(groupedItems)
            }
        }

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        transactionViewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.tvBalance.text = formatter.format(balance ?: 0.0)
        }

        transactionViewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = "+${formatter.format(income ?: 0.0)}"
            updateProgressBar()
        }

        transactionViewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvExpense.text = "-${formatter.format(expense ?: 0.0)}"
            updateProgressBar()
        }
    }

    private fun updateProgressBar() {
        val totalIncome = transactionViewModel.totalIncome.value ?: 0.0
        val totalExpense = transactionViewModel.totalExpense.value ?: 0.0

        if (totalIncome > 0) {
            binding.incomeProgress.max = totalIncome.toInt()
            binding.expenseProgress.max = totalIncome.toInt()
            binding.incomeProgress.progress = totalIncome.toInt()
            binding.expenseProgress.progress = totalExpense.toInt()
        } else {
            binding.incomeProgress.max = 1
            binding.expenseProgress.max = 1
            binding.incomeProgress.progress = 0
            binding.expenseProgress.progress = 0
        }
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