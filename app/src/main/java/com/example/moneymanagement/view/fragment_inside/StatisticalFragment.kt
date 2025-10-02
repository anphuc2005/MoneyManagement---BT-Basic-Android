package com.example.moneymanagement.view.fragment_inside

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moneymanagement.databinding.FragmentStatisticalBinding
import com.google.android.material.tabs.TabLayoutMediator

class StatisticalFragment : Fragment() {

    private var _binding: FragmentStatisticalBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = arrayOf("Thu nhập", "Chi tiêu")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = object : FragmentStateAdapter(requireActivity() as FragmentActivity) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> StatisticalIncomeFragment()
                    1 -> StatisticalExpenseFragment()
                    else -> StatisticalIncomeFragment()
                }
            }
        }

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
