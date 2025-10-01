package com.example.moneymanagement.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoggedIn = checkLoginStatus()

        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()
    }

    private fun checkLoginStatus(): Boolean {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPref.getBoolean("is_logged_in", false)
    }
}
