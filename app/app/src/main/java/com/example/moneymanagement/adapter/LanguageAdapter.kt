package com.example.moneymanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanagement.data.data_class.Language
import com.example.moneymanagement.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val onLanguageClick: (Language) -> Unit
) : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {

    private var selectedLanguageCode: String = ""

    fun setSelectedLanguage(code: String) {
        selectedLanguageCode = code
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(language: Language) {
            binding.apply {
                tvLanguageName.text = language.name
                ivFlag.setImageResource(language.flagResId)

                ivSelected.visibility = if (language.code == selectedLanguageCode) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                root.setOnClickListener {
                    onLanguageClick(language)
                }
            }
        }
    }

    class LanguageDiffCallback : DiffUtil.ItemCallback<Language>() {
        override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem == newItem
        }
    }
}