package com.example.moneymanagement.view.fragment_login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.moneymanagement.R
import com.example.moneymanagement.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun resetPassword() {
        val email = binding.emailInput.text.toString().trim()

        if(email.isEmpty()) {
            binding.emailInput.error = "Vui lòng nhập email"
            binding.emailInput.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Email không hợp lệ"
            binding.emailInput.requestFocus()
            return
        }

        binding.btnResetPassword.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (!isAdded) return@addOnCompleteListener

                binding.btnResetPassword.isEnabled = true
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Email đặt lại mật khẩu đã được gửi đến $email",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().popBackStack()
                } else {
                    val errorMessage = when (task.exception?.message) {
                        "There is no user record corresponding to this identifier. The user may have been deleted." ->
                            "Email không tồn tại trong hệ thống"
                        "The email address is badly formatted." ->
                            "Email không đúng định dạng"
                        else ->
                            "Lỗi: ${task.exception?.message}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}