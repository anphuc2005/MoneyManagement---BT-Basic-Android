package com.example.moneymanagement.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Kiểm tra từ Firebase Auth trước
        val currentUser = auth.currentUser

        if (currentUser != null) {
            saveUserInfoIfNeeded(currentUser)
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }
        }
        finish()
    }

    private fun saveUserInfoIfNeeded(user: com.google.firebase.auth.FirebaseUser) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val storedUserId = sharedPref.getString("user_id", null)

        if (storedUserId != user.uid) {
            val displayName = user.displayName
                ?: user.email?.substringBefore("@")
                ?: "User"

            sharedPref.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_id", user.uid)
                putString("user_email", user.email)
                putString("user_name", displayName)
                apply()
            }
        }
    }
}