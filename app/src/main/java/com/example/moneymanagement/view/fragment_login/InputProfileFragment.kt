package com.example.moneymanagement.view.fragment_login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moneymanagement.R
import com.example.moneymanagement.data.data_class.UserManager
import com.example.moneymanagement.data.data_class.UserProfile
import com.example.moneymanagement.databinding.FragmentInputProfileBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputProfileFragment : Fragment() {

    private var _binding: FragmentInputProfileBinding? = null
    private val binding get() = _binding!!
    private val TAG = "InputProfileFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkFirebaseAuth()
        setupListener()
        loadExistingProfile()
    }

    private fun checkFirebaseAuth() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d(TAG, "Current user: ${currentUser?.uid}")
        Log.d(TAG, "User email: ${currentUser?.email}")

        if (currentUser == null) {
            Log.w(TAG, "No user logged in!")
        }
    }

    private fun setupListener() {
        binding.btnContinue.setOnClickListener {
            submitData()
        }

        binding.dateInputLayout.setEndIconOnClickListener {
            showDatePicker()
        }

        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadExistingProfile() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading existing profile...")
                val result = UserManager.getUserProfile()
                result.onSuccess { profile ->
                    profile?.let {
                        Log.d(TAG, "Profile loaded: ${it.fullName}")
                        binding.nameEditText.setText(it.fullName)
                        binding.dateEditText.setText(it.dateOfBirth)
                        binding.descriptonEditText.setText(it.description)
                    } ?: Log.d(TAG, "No existing profile found")
                }
                result.onFailure {
                    Log.e(TAG, "Error loading profile: ${it.message}", it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading profile: ${e.message}", e)
            }
        }
    }

    private fun submitData() {
        val fullName = binding.nameEditText.text.toString().trim()
        val dateOfBirth = binding.dateEditText.text.toString().trim()
        val description = binding.descriptonEditText.text.toString().trim()

        Log.d(TAG, "Submit data - Name: $fullName, DOB: $dateOfBirth")

        // Validation
        var hasError = false

        if (fullName.isEmpty()) {
            binding.nameInputLayout.error = "Vui lòng nhập họ tên"
            hasError = true
        } else {
            binding.nameInputLayout.error = null
        }

        if (dateOfBirth.isEmpty()) {
            binding.dateInputLayout.error = "Vui lòng chọn ngày sinh"
            hasError = true
        } else {
            binding.dateInputLayout.error = null
        }

        if (description.isEmpty()) {
            binding.descriptonInputLayout.error = "Vui lòng nhập mô tả"
            hasError = true
        } else {
            binding.descriptonInputLayout.error = null
        }

        if (hasError) {
            Log.d(TAG, "Validation failed")
            return
        }

        if (!UserManager.isUserLoggedIn()) {
            Log.e(TAG, "User not logged in!")
            Toast.makeText(requireContext(), "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show()
            // Optional: Navigate to login
            // findNavController().navigate(R.id.action_to_loginFragment)
            return
        }

        setLoadingState(true)

        saveProfileToFirebase(fullName, dateOfBirth, description)
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnContinue.isEnabled = !isLoading
        binding.btnContinue.text = if (isLoading) "Đang lưu..." else "Tiếp tục"

        binding.nameEditText.isEnabled = !isLoading
        binding.dateEditText.isEnabled = !isLoading
        binding.descriptonEditText.isEnabled = !isLoading
        binding.backBtn.isEnabled = !isLoading
    }

    private fun saveProfileToFirebase(fullName: String, dateOfBirth: String, description: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting save to Firebase...")

                val userId = UserManager.getCurrentUserId()
                if (userId == null) {
                    Log.e(TAG, "User ID is null!")
                    showError("Không thể lấy thông tin user")
                    return@launch
                }

                Log.d(TAG, "User ID: $userId")

                val userProfile = UserProfile(
                    userId = userId,
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    description = description,
                    email = UserManager.getUserEmail() ?: "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                Log.d(TAG, "Saving profile: $userProfile")

                val result = withTimeout(10000) {
                    UserManager.saveUserProfile(userProfile)
                }

                result.onSuccess {
                    Log.d(TAG, "Profile saved successfully!")

                    saveToSharedPreferences(fullName)

                    Toast.makeText(requireContext(), "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show()

                    try {
                        findNavController().navigate(R.id.action_inputProfileFragment_to_loginFragment)
                    } catch (e: Exception) {
                        Log.e(TAG, "Navigation error: ${e.message}", e)
                        findNavController().popBackStack()
                    }
                }

                result.onFailure { exception ->
                    Log.e(TAG, "Error saving profile: ${exception.message}", exception)
                    showError("Lỗi khi lưu: ${exception.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception in saveProfileToFirebase: ${e.message}", e)
                showError("Có lỗi xảy ra: ${e.message}")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun saveToSharedPreferences(fullName: String) {
        try {
            val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            sharedPref.edit().apply {
                putString("user_name", fullName)
                apply()
            }
            Log.d(TAG, "Saved to SharedPreferences: $fullName")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to SharedPreferences: ${e.message}", e)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        setLoadingState(false)
        Log.e(TAG, "Error shown to user: $message")
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày sinh")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(parentFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.dateEditText.setText(formatter.format(Date(selection)))
            binding.dateInputLayout.error = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}