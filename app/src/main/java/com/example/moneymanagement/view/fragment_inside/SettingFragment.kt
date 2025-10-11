package com.example.moneymanagement.view.fragment_inside

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.R
import com.example.moneymanagement.databinding.FragmentSettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
            findNavController().popBackStack()
        }

        binding.cardLanguage.setOnClickListener {

        }

        binding.cardCategory.setOnClickListener {
            Toast.makeText(requireContext(), "Quản lý thẻ loại", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_setting_to_typeManagement)
        }

        binding.cardTheme.setOnClickListener {
            showThemeBottomSheet()
        }

        binding.cardAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Tài khoản", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_setting_to_account)
        }
    }

    private fun showThemeBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_theme_selection, null)

        val btnClose = bottomSheetView.findViewById<ImageView>(R.id.btn_close)
        val radioLight = bottomSheetView.findViewById<RadioButton>(R.id.radio_light)
        val radioDark = bottomSheetView.findViewById<RadioButton>(R.id.radio_dark)
        val radioSystem = bottomSheetView.findViewById<RadioButton>(R.id.radio_system)

        // Lấy theme hiện tại từ SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        val currentTheme = sharedPref.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Set checked cho radio button tương ứng
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> radioLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> radioDark.isChecked = true
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> radioSystem.isChecked = true
        }

        // Thêm listener cho từng radio button để thay đổi màu khi click
        radioLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTheme(AppCompatDelegate.MODE_NIGHT_NO)
                bottomSheetDialog.dismiss()
            }
        }

        radioDark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTheme(AppCompatDelegate.MODE_NIGHT_YES)
                bottomSheetDialog.dismiss()
            }
        }

        radioSystem.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                bottomSheetDialog.dismiss()
            }
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun applyTheme(themeMode: Int) {
        val sharedPref = requireActivity().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().putInt("theme_mode", themeMode).apply()

        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SettingFragment()
    }
}