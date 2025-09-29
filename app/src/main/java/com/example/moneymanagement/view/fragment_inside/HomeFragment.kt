package com.example.moneymanagement.view.fragment_inside

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.TransactionAdapter
import com.example.moneymanagement.data.data_class.TransactionGroupHelper
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionWithCategory
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentHomeBinding
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            showTransactionDetails(transaction)
        }

        binding.transactionRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(context)

        }
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
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Minh Hoa") ?: "Minh Hoa"
        binding.userName.text = userName
    }

    private fun updateChartData(transactions: List<TransactionWithCategory>) {
        val incomeTotal = transactions
            .filter { it.transaction.type == com.example.moneymanagement.data.model.TransactionType.INCOME }
            .sumOf { it.transaction.amount }

        val expenseTotal = transactions
            .filter { it.transaction.type == com.example.moneymanagement.data.model.TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }

        // TODO: Update chart with data
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