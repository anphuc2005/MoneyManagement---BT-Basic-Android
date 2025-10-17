package com.example.moneymanagement.view.fragment_inside

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moneymanagement.R
import com.example.moneymanagement.databinding.FragmentStatisticalBinding
import com.google.android.material.tabs.TabLayoutMediator

class StatisticalFragment : Fragment() {

    private var _binding: FragmentStatisticalBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabTitles: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabTitles = arrayOf(
            getString(R.string.tab_income),
            getString(R.string.tab_expense)
        )

        val adapter = StatisticalPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class StatisticalPagerAdapter(
        fragment: Fragment
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> StatisticalIncomeFragment()
                1 -> StatisticalExpenseFragment()
                else -> StatisticalIncomeFragment()
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun containsItem(itemId: Long): Boolean {
            return itemId >= 0 && itemId < itemCount
        }
    }
}