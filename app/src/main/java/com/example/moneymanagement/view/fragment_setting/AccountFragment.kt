package com.example.moneymanagement.view.fragment_setting

import android.Manifest
import android.R
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
import com.bumptech.glide.Glide
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
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Cần quyền truy cập ảnh", Toast.LENGTH_SHORT).show()
        }
    }

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
            findNavController().popBackStack()
        }

        binding.fabEditAvatar.setOnClickListener {
            checkPermissionAndPickImage()
        }

        binding.etDateOfBirth.setOnClickListener {
            if (isEditMode) showDatePicker()
        }

        binding.tilDateOfBirth.setEndIconOnClickListener {
            if (isEditMode) showDatePicker()
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
        transactionViewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            val formatted = formatCurrency(income)
            binding.tvTotalIncome.text = "+$formatted"
        }

        transactionViewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            val formatted = formatCurrency(expense)
            binding.tvTotalExpense.text = "-$formatted"
        }

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


        lifecycleScope.launch {
            try {
                val avatarUrl = selectedImageUri?.let { uploadAvatar(it) } ?: userProfile?.avatarUrl.orEmpty()

                val updates = mutableMapOf<String, Any>(
                    "fullName" to binding.etFullName.text.toString().trim(),
                    "dateOfBirth" to binding.etDateOfBirth.text.toString().trim(),
                    "description" to binding.etDescription.text.toString().trim(),
                    "avatarUrl" to avatarUrl
                )

                val result = UserManager.updateUserProfile(updates)
                result.onSuccess {
                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    loadUserProfile()
                }.onFailure {
                    Toast.makeText(requireContext(), "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun uploadAvatar(uri: Uri): String? {
        return try {
            val userId = UserManager.getCurrentUserId() ?: run {
                Toast.makeText(requireContext(), "Không tìm thấy userId", Toast.LENGTH_SHORT).show()
                return null
            }

            val timestamp = System.currentTimeMillis()
            val storageRef = FirebaseStorage.getInstance().reference
            val avatarRef = storageRef.child("avatars/${userId}_${timestamp}.jpg")

            android.util.Log.d("AccountFragment", "Upload path: avatars/${userId}_${timestamp}.jpg")

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }

            android.util.Log.d("AccountFragment", "Bitmap created: ${bitmap.width}x${bitmap.height}")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()

            android.util.Log.d("AccountFragment", "Image size: ${data.size} bytes")

            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            val uploadTask = avatarRef.putBytes(data, metadata)
            val taskSnapshot = uploadTask.await()

            android.util.Log.d("AccountFragment", "Upload complete: ${taskSnapshot.metadata?.path}")

            kotlinx.coroutines.delay(500)

            val downloadUrl = taskSnapshot.storage.downloadUrl.await()
            android.util.Log.d("AccountFragment", "Download URL: $downloadUrl")
            downloadUrl.toString()

        } catch (e: Exception) {
            android.util.Log.e("AccountFragment", "Upload avatar error", e)
            Toast.makeText(
                requireContext(),
                "Chi tiết lỗi: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
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