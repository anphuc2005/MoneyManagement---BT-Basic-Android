package com.example.moneymanagement.view.fragment_setting

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.LanguageAdapter
import com.example.moneymanagement.data.data_class.Language
import com.example.moneymanagement.databinding.FragmentLanguageBinding
import java.util.Locale

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private lateinit var languageAdapter: LanguageAdapter
    private lateinit var currentLanguageCode: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCurrentLanguage()
        setupRecyclerView()
        setupClickListeners()
        loadLanguages()
    }

    private fun getCurrentLanguage() {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        currentLanguageCode = sharedPref.getString("language_code", "vi") ?: "vi"
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter { language ->
            changeLanguage(language)
        }

        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = languageAdapter
        }

        languageAdapter.setSelectedLanguage(currentLanguageCode)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadLanguages() {
        val languages = listOf(
            Language("vi", "Việt Nam", R.drawable.flag_vietnam, "vi"),
            Language("en", "English (UK)", R.drawable.flag_uk, "en"),
            Language("en-US", "English (US)", R.drawable.flag_us, "en"),
            Language("nl", "Belgium (Dutch)", R.drawable.flag_belgium, "nl"),
            Language("fr", "French", R.drawable.flag_french, "fr"),
            Language("ko", "Korea", R.drawable.flag_korea, "ko"),
            Language("de", "German", R.drawable.flag_germany, "de"),
            Language("ja", "Japanese", R.drawable.flag_japan, "ja"),
            Language("zh-CN", "Chinese (Simplified)", R.drawable.flag_china, "zh_CN")
        )

        languageAdapter.submitList(languages)
    }

    private fun changeLanguage(language: Language) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("language_code", language.code).apply()

        currentLanguageCode = language.code
        languageAdapter.setSelectedLanguage(currentLanguageCode)

        setLocale(language.localeString)

        requireActivity().recreate()
    }

    private fun setLocale(languageCode: String) {
        val locale = if (languageCode.contains("_")) {
            val parts = languageCode.split("_")
            Locale(parts[0], parts[1])
        } else {
            Locale(languageCode)
        }

        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        requireActivity().resources.updateConfiguration(
            config,
            requireActivity().resources.displayMetrics
        )

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("language", languageCode).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}