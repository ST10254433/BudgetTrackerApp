package com.example.budgettrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            if (username.text.isNotBlank() && password.text.isNotBlank()) {
                // Save login status
                val prefs = getSharedPreferences("budget_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("loggedIn", true).apply()

                startActivity(Intent(this, DashboardActivity::class.java))
                finish() // Prevent going back to log with back button
            } else {
                Toast.makeText(this, "Enter credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
