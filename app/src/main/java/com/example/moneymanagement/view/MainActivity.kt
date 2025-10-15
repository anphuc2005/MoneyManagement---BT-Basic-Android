package com.example.moneymanagement.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moneymanagement.R
import com.example.moneymanagement.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        loadLocale()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.addTransactionFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.fabAdd.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.fabAdd.visibility = View.VISIBLE
                }
            }
        }

        binding.fabAdd.setOnClickListener {
            if (navController.currentDestination?.id != R.id.addTransactionFragment) {
                navController.navigate(R.id.addTransactionFragment)
            }
        }
    }

    private fun loadLocale() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPref.getString("language", "vi") ?: "vi"

        val locale = if (languageCode.contains("_")) {
            val parts = languageCode.split("_")
            Locale(parts[0], parts[1])
        } else {
            Locale(languageCode)
        }

        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }


    fun getNavController(): NavController = navController


}
