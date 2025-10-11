package com.example.moneymanagement.view.fragment_setting

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moneymanagement.data.data_class.UserManager
import com.example.moneymanagement.data.data_class.UserProfile
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentAccountBinding
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private var userProfile: UserProfile? = null
    private var isEditMode = false
    private var selectedImageUri: Uri? = null
    private var selectedDateOfBirth: Date? = null

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Cần quyền truy cập ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivAvatar.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupClickListeners()
        loadUserProfile()
        observeTransactionData()
    }

    private fun setupViewModel() {
        val database = TransactionDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao(), database.categoryDao())
        val factory = TransactionViewModelFactory(repository)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        UserManager.getCurrentUserId()?.let { userId ->
            transactionViewModel.setUserId(userId)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            findNavController().popBackStack()
        }


        binding.fabEditAvatar.setOnClickListener {
            if (isEditMode) {
                checkPermissionAndPickImage()
            }
        }

        binding.etDateOfBirth.setOnClickListener {
            if (isEditMode) {
                showDatePicker()
            }
        }

        binding.tilDateOfBirth.setEndIconOnClickListener {
            if (isEditMode) {
                showDatePicker()
            }
        }

        binding.btnCancel.setOnClickListener {
            toggleEditMode(false)
            loadUserProfile() // Reload data
        }

        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val result = UserManager.getUserProfile()
            result.onSuccess { profile ->
                profile?.let {
                    userProfile = it
                    displayUserProfile(it)
                } ?: run {
                    // Create default profile if not exists
                    createDefaultProfile()
                }
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi tải thông tin: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayUserProfile(profile: UserProfile) {
        binding.etFullName.setText(profile.fullName)
        binding.etEmail.setText(profile.email)
        binding.etDateOfBirth.setText(profile.dateOfBirth)
        binding.etDescription.setText(profile.description)

        // Load avatar if exists
        // TODO: Implement avatar loading from Firebase Storage
    }

    private fun createDefaultProfile() {
        val userId = UserManager.getCurrentUserId() ?: return
        val email = UserManager.getUserEmail() ?: ""

        val defaultProfile = UserProfile(
            userId = userId,
            fullName = UserManager.getUserDisplayName() ?: "",
            email = email,
            dateOfBirth = "",
            description = ""
        )

        lifecycleScope.launch {
            UserManager.saveUserProfile(defaultProfile)
            userProfile = defaultProfile
            displayUserProfile(defaultProfile)
        }
    }

    private fun observeTransactionData() {
        // Observe total income
        transactionViewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            val formatted = formatCurrency(income)
            binding.tvTotalIncome.text = "+$formatted"
        }

        // Observe total expense
        transactionViewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            val formatted = formatCurrency(expense)
            binding.tvTotalExpense.text = "-$formatted"
        }

        // Observe balance
        transactionViewModel.balance.observe(viewLifecycleOwner) { balance ->
            val formatted = formatCurrency(balance)
            val prefix = if (balance >= 0) "+" else ""
            binding.tvBalance.text = "$prefix$formatted"
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${formatter.format(amount)} VNĐ"
    }

    private fun toggleEditMode(enabled: Boolean) {
        isEditMode = enabled

        // Enable/disable input fields
        binding.etFullName.isEnabled = enabled
        binding.etDateOfBirth.isEnabled = enabled
        binding.etDescription.isEnabled = enabled


        // Reset selected image if cancel
        if (!enabled) {
            selectedImageUri = null
        }
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Chọn ngày sinh")

        // Set current selected date or today
        val selectedTime = selectedDateOfBirth?.time ?: System.currentTimeMillis()
        builder.setSelection(selectedTime)

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDateOfBirth = Date(selection)
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
            binding.etDateOfBirth.setText(formatter.format(selectedDateOfBirth!!))
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun saveUserProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val dateOfBirth = binding.etDateOfBirth.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Vui lòng nhập tên"
            return
        }

        binding.tilFullName.error = null

        // Show loading
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Đang lưu..."

        lifecycleScope.launch {
            try {
                // Upload avatar if selected
                var avatarUrl: String? = null
                if (selectedImageUri != null) {
                    avatarUrl = uploadAvatar(selectedImageUri!!)
                }

                // Update profile
                val updates = mutableMapOf<String, Any>(
                    "fullName" to fullName,
                    "dateOfBirth" to dateOfBirth,
                    "description" to description
                )

                avatarUrl?.let {
                    updates["avatarUrl"] = it
                }

                val result = UserManager.updateUserProfile(updates)

                result.onSuccess {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    toggleEditMode(false)
                    loadUserProfile()
                }.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "LƯU"
            }
        }
    }

    private suspend fun uploadAvatar(uri: Uri): String? {
        return try {
            val userId = UserManager.getCurrentUserId() ?: return null
            val storageRef = FirebaseStorage.getInstance().reference
            val avatarRef = storageRef.child("avatars/$userId.jpg")

            // Compress image
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()

            // Upload
            val uploadTask = avatarRef.putBytes(data)
            uploadTask.await()

            // Get download URL
            val downloadUrl = avatarRef.downloadUrl.await()
            downloadUrl.toString()

        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AccountFragment()
    }
}