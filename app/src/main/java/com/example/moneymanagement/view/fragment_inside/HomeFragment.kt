package com.example.moneymanagement.view.fragment_inside

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.TransactionAdapter
import com.example.moneymanagement.data.data_class.LineChartHelper
import com.example.moneymanagement.data.data_class.TransactionGroupHelper
import com.example.moneymanagement.data.data_class.UserManager
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentHomeBinding
import com.example.moneymanagement.databinding.BottomDialogTransactionOptionsBinding
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        updateUserName()
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
        transactionAdapter = TransactionAdapter(
            onItemClick = { transaction ->
                showTransactionDetails(transaction)
            },
            onItemLongClick = { transaction ->
                showTransactionOptionsDialog(transaction)
            }
        )

        binding.transactionRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showTransactionOptionsDialog(transaction: TransactionWithCategory) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogBinding = BottomDialogTransactionOptionsBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(dialogBinding.root)

        dialogBinding.apply {
            optionDelete.setOnClickListener {
                deleteTransaction(transaction)
                bottomSheetDialog.dismiss()
            }

            optionEdit.setOnClickListener {
                editTransaction(transaction)
                bottomSheetDialog.dismiss()
            }

            optionOut.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

        }

        bottomSheetDialog.show()
    }

    private fun deleteTransaction(transaction: TransactionWithCategory) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    try {
                        transactionViewModel.deleteTransaction(transaction.transaction)
                        Toast.makeText(requireContext(), "Đã xóa giao dịch", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi khi xóa giao dịch: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun editTransaction(transaction: TransactionWithCategory) {
        val action = HomeFragmentDirections.actionNavOverviewToEditTransactionFragment(
            transactionId = transaction.transaction.id
        )
        findNavController().navigate(action)
    }

    private fun observeData() {
        transactionViewModel.allTransaction.observe(viewLifecycleOwner) { transactions ->
            Log.d("HomeFragment", "Received transactions: ${transactions?.size}")
            transactions?.forEach {
                Log.d("HomeFragment", "Transaction: ${it.transaction.transaction_name}, Amount: ${it.transaction.amount}, Date: ${it.transaction.date}")
            }

            if (transactions.isNullOrEmpty()) {
                binding.transactionRecyclerView.visibility = View.GONE
            } else {
                Log.d("HomeFragment", "Showing ${transactions.size} transactions")
                binding.transactionRecyclerView.visibility = View.VISIBLE

                Log.d("HomeFragment", "Adapter item count before: ${transactionAdapter.itemCount}")
                val groupedItems = TransactionGroupHelper.groupTransactionsByDate(transactions)

                transactionAdapter.submitList(groupedItems) {
                    Log.d("HomeFragment", "Adapter item count after submitList completed: ${transactionAdapter.itemCount}")
                    binding.transactionRecyclerView.scrollToPosition(0)
                }

                updateChartData(transactions)
            }
        }

        lifecycleScope.launch {
            val summary = transactionViewModel.getTransactionSummary()
            updateSummaryUI(summary)
        }
    }

    private fun updateUserName() {
        val firebaseDisplayName = UserManager.getUserDisplayName()
        val firebaseEmail = UserManager.getUserEmail()

        val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val storedName = sharedPref.getString("user_name", null)

        val displayName = firebaseDisplayName
            ?: storedName
            ?: firebaseEmail?.substringBefore("@")
            ?: "User"

        binding.userName.text = displayName

        Log.d("HomeFragment", "Display name: $displayName")
    }

    private fun updateChartData(transactions: List<TransactionWithCategory>) {
        val incomeTotal = transactionViewModel.totalIncome.value ?: 0.0
        val expenseTotal = transactionViewModel.totalExpense.value ?: 0.0

        Log.d("HomeFragment", "Income: $incomeTotal, Expense: $expenseTotal")

        // Setup LineChart với dữ liệu thực
        LineChartHelper.setupLineChart(binding.chartContainer, transactions)
    }

    private fun updateSummaryUI(summary: com.example.moneymanagement.data.model.TransactionSummary) {
        // TODO: Update summary UI
    }

    private fun showTransactionDetails(transaction: TransactionWithCategory) {
        // TODO: Show transaction details
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val summary = transactionViewModel.getTransactionSummary()
            updateSummaryUI(summary)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}