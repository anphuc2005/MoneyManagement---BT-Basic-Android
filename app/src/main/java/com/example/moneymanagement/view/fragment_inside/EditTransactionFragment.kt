package com.example.moneymanagement.view.fragment_inside

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.CategorySelectionAdapter
import com.example.moneymanagement.data.data_class.UserManager
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.model.Transactions
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentEditTransactionBinding
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionFragment : Fragment() {
    private var _binding: FragmentEditTransactionBinding? = null
    private val binding get() = _binding!!

    private val args: EditTransactionFragmentArgs by navArgs()
    private lateinit var transactionViewModel: TransactionViewModel

    private var selectedCategoryId: Int = 0
    private var selectedTransactionType: TransactionType = TransactionType.EXPENSE
    private var transactionId: Long = 0

    private var currentCategories: List<Category> = emptyList()

    private var selectedCategory: Category? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupClickListeners()
        loadTransactionData()
    }

    private fun setupViewModel() {
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao(), database.categoryDao())
        val factory = TransactionViewModelFactory(repository)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        val userId = UserManager.getCurrentUserId()
        if (userId != null) {
            transactionViewModel.setUserId(userId)
        }
    }

    private fun loadTransactionData() {
        transactionId = args.transactionId

        lifecycleScope.launch {
            val transaction = transactionViewModel.getTransactionById(transactionId)
            transaction?.let { fillFormWithData(it) }
        }
    }

    private fun fillFormWithData(transaction: Transactions) {
        binding.apply {
            etTransactionName.setText(transaction.transaction_name)
            etAmount.setText(transaction.amount.toString())
            etDate.setText(transaction.date)
            etNote.setText(transaction.note)

            selectedCategoryId = transaction.category_id
            selectedTransactionType = transaction.type


            lifecycleScope.launch {
                val category = transactionViewModel.getCategoryById(transaction.category_id)
                etCategory.setText(category?.type_name ?: "Unknown")
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnCancel.setOnClickListener {
                findNavController().popBackStack()
            }

            btnSave.setOnClickListener {
                updateTransaction()
            }

            etDate.setOnClickListener {
                showDatePicker()
            }

            tilDate.setEndIconOnClickListener {
                showDatePicker()
            }

            etCategory.setOnClickListener {
                showCategorySelectionDialog()
            }

            tilCategory.setEndIconOnClickListener {
                showCategorySelectionDialog()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val existingDate = binding.etDate.text.toString()
            if (existingDate.isNotEmpty()) {
                calendar.time = dateFormat.parse(existingDate) ?: Date()
            }
        } catch (e: Exception) {
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = dateFormat.format(calendar.time)
                binding.etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun showCategorySelectionDialog() {
        if (currentCategories.isEmpty()) {
            Toast.makeText(context, "Đang tải danh mục...", Toast.LENGTH_SHORT).show()
            return
        }

        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_category_selection, null)

        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.rv_categories)
        val btnClose = bottomSheetView.findViewById<ImageView>(R.id.btn_close)

        val adapter = CategorySelectionAdapter { category ->
            selectedCategory = category
            binding.etCategory.setText(category.type_name)
            binding.tilCategory.error = null
            bottomSheetDialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.submitList(currentCategories.toList())

        Log.d("AddTransactionFragment", "Showing ${currentCategories.size} categories in bottom sheet")

        btnClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun updateTransaction() {
        val transactionName = binding.etTransactionName.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        if (transactionName.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên giao dịch", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategoryId == 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = UserManager.getCurrentUserId()
        if (userId == null) {
            Toast.makeText(requireContext(), "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTransaction = Transactions(
            id = transactionId,
            user_id = userId,
            category_id = selectedCategoryId,
            transaction_name = transactionName,
            amount = amount,
            date = date,
            note = note,
            type = selectedTransactionType,
            createdAt = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            try {
                transactionViewModel.updateTransaction(updatedTransaction)
                Toast.makeText(requireContext(), "Cập nhật giao dịch thành công", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi khi cập nhật giao dịch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}