package com.example.moneymanagement.view.fragment_inside

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.CategorySelectionAdapter
import com.example.moneymanagement.data.data_class.UserManager
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentAddTransactionBinding
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel

    private var selectedCategory: Category? = null
    private var selectedDate: Date = Date()
    private var currentTransactionType = TransactionType.EXPENSE

    private var currentCategories: List<Category> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        setupClickListener()
        observeCategories()
    }

    private fun setupViewModel() {
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao(), database.categoryDao())
        val factory = TransactionViewModelFactory(repository)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
    }

    private fun setupUI() {
        updateDateDisplay()
    }

    private fun setupClickListener() {
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.tilDate.setEndIconOnClickListener {
            showDatePicker()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etCategory.setOnClickListener {
            showCategorySelection()
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeCategories() {
        transactionViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            currentCategories = categories
            Log.d("AddTransactionFragment", "Categories updated: ${categories.size} items")
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Chọn ngày giao dịch")
        builder.setSelection(selectedDate.time)

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Date(selection)
            updateDateDisplay()
            binding.tilDate.error = null
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun updateDateDisplay() {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        binding.etDate.setText(formatter.format(selectedDate))
    }

    private fun saveTransaction() {
        val currentUserId = UserManager.getCurrentUserId()
        if (currentUserId == null) {
            Toast.makeText(context, "Không thể xác thực người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionName = binding.etTransactionName.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()
        val note = binding.etNote.text.toString().trim()
        val date = binding.etDate.text.toString().trim()

        if (!validateInput(transactionName, amountText)) {
            return
        }

        val amount = amountText.toDouble()

        transactionViewModel.insertTransaction(
            transaction_name = transactionName,
            amount = amount,
            category_id = selectedCategory?.id ?: 1,
            date = date,
            userId = currentUserId,
            note = note,
        )

        Toast.makeText(context, "Đã thêm giao dịch thành công", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun validateInput(transactionName: String, amountText: String): Boolean {
        var isValid = true

        binding.tilTransactionName.error = null
        binding.tilAmount.error = null
        binding.tilCategory.error = null

        if (transactionName.isEmpty()) {
            binding.tilTransactionName.error = "Vui lòng nhập tên giao dịch"
            isValid = false
        }

        if (amountText.isEmpty()) {
            binding.tilAmount.error = "Vui lòng nhập số tiền"
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    binding.tilAmount.error = "Số tiền phải lớn hơn 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.tilAmount.error = "Số tiền không hợp lệ"
                isValid = false
            }
        }

        return isValid
    }

    private fun showCategorySelection() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}