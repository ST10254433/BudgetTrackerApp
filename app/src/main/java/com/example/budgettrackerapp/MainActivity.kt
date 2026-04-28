package com.example.budgettrackerapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logo = findViewById<ImageView>(R.id.splashLogo)
        val text = findViewById<TextView>(R.id.splashText)

        // Logo slides up + fades in
        val slideUpFade = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)
        logo.startAnimation(slideUpFade)

        // Text fades + scales in
        val fadeScale = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        text.startAnimation(fadeScale)

        // Delay then check login status
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("budget_prefs", MODE_PRIVATE)
            val loggedIn = prefs.getBoolean("loggedIn", false)

            if (loggedIn) {
                val intent = Intent(this, DashboardActivity::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    logo,
                    "app_logo" // shared element
                )
                startActivity(intent, options.toBundle())
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()
        }, 2000) // 2 seconds splash
    }
}
