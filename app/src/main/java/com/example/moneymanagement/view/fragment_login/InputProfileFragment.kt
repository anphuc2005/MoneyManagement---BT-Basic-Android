package com.example.moneymanagement.view.fragment_login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.moneymanagement.R
import com.example.moneymanagement.databinding.FragmentInputProfileBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputProfileFragment : Fragment() {

    private var _binding: FragmentInputProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
    }

    private fun setupListener() {
        binding.btnContinue.setOnClickListener {
            submitData()
            findNavController().navigate(R.id.action_inputProfileFragment_to_loginFragment)
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

    private fun submitData() {
        val fullName = binding.nameEditText.text.toString().trim()
        val dateOfBirth = binding.dateEditText.text.toString().trim()
        val description = binding.descriptonEditText.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.nameInputLayout.error = "Please enter your full name"
            return
        }

        if (dateOfBirth.isEmpty()) {
            binding.dateInputLayout.error = "Please select your date of birth"
            return
        }

        if (description.isEmpty()) {
            binding.descriptonInputLayout.error = "Please enter a description"
            return
        }

        binding.nameInputLayout.error = null
        binding.dateInputLayout.error = null
        binding.descriptonInputLayout.error = null

        // TODO: Handle data submission (save to database, navigate to next screen, etc.)
        // Example: navigate to next fragment or activity
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Set default selection
            .build()

        datePicker.show(parentFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.dateEditText.setText(formatter.format(Date(selection)))
            binding.dateInputLayout.error = null
        }

        datePicker.addOnNegativeButtonClickListener {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}