package com.example.moneymanagement.view.fragment_inside

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeData()
        setupClickListeners()
    }

    private fun setupViewModel() {
        val datebase = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(datebase.transactionDao(), datebase.categoryDao())
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
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeData() {
        transactionViewModel.allTransaction.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
//            updateChartData(transactions)
        }

        lifecycleScope.launch {
            val summary = transactionViewModel.getTransactionSummary()
//            updateSummaryUI(summary)
        }
    }

    private fun setupClickListeners() {
//        binding.fabAdd.setOnClickListener {
//            // Navigate to Add Transaction screen
//            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
//        }
    }

//    private fun updateUserName() {
//        // Get user name from preferences or database
//        val userName = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//            .getString("user_name", "Minh Hoa") ?: "Minh Hoa"
//        binding.userName.text = userName
//    }

    private fun showTransactionDetails(transaction: TransactionWithCategory) {
        // Navigate to transaction detail screen or show dialog
//        val action = HomeFragmentDirections.actionHomeToTransactionDetail(transaction.transaction.id)
//        findNavController().navigate(action)
    }


}