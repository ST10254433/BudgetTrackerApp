package com.example.budgettrackerapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var totalExpensesLabel: TextView
    private lateinit var goalLabel: TextView
    private lateinit var badgeText: TextView
    private lateinit var goalProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Transition for logo morph
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)

        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(findViewById(R.id.toolbar))

        db = AppDatabase.getDatabase(this)

        totalExpensesLabel = findViewById(R.id.totalExpensesLabel)
        goalLabel = findViewById(R.id.goalLabel)
        badgeText = findViewById(R.id.badgeText)
        goalProgress = findViewById(R.id.goalProgress)

        val addExpenseButton = findViewById<Button>(R.id.addExpenseButton)
        val viewExpensesButton = findViewById<Button>(R.id.viewExpensesButton)
        val addCategoryButton = findViewById<Button>(R.id.addCategoryButton)
        val graphButton = findViewById<Button>(R.id.graphButton)
        val pdfButton = findViewById<Button>(R.id.pdfButton)
        val darkModeButton = findViewById<Button>(R.id.darkModeButton)

        // Animate buttons on load
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        addExpenseButton.startAnimation(fadeIn)
        viewExpensesButton.startAnimation(fadeIn)
        addCategoryButton.startAnimation(fadeIn)
        graphButton.startAnimation(fadeIn)
        pdfButton.startAnimation(fadeIn)
        darkModeButton.startAnimation(fadeIn)

        addExpenseButton.setOnClickListener {
            startActivity(Intent(this, ExpenseEntryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        viewExpensesButton.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        addCategoryButton.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        graphButton.setOnClickListener {
            startActivity(Intent(this, GraphActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        pdfButton.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        darkModeButton.setOnClickListener {
            val prefs = getSharedPreferences("budget_prefs", MODE_PRIVATE)
            val editor = prefs.edit()
            val current = prefs.getBoolean("darkMode", false)
            if (current) {
                editor.putBoolean("darkMode", false).apply()
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                )
            } else {
                editor.putBoolean("darkMode", true).apply()
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                )
            }
        }

        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        CoroutineScope(Dispatchers.IO).launch {
            val total = db.expenseDao().getTotalExpenses() ?: 0.0
            val prefs = getSharedPreferences("budget_prefs", MODE_PRIVATE)
            val maxGoal = prefs.getInt("maxGoal", 10000)
            val minGoal = prefs.getInt("minGoal", 0)

            runOnUiThread {
                totalExpensesLabel.text = "Total Expenses: R$total"
                goalLabel.text = "Monthly Goal: R$maxGoal"

                val range = maxGoal - minGoal
                val performance = if (range != 0) {
                    (((total - minGoal).toFloat() / range) * 100).toInt()
                } else {
                    if (total >= maxGoal) 100 else 0
                }

                // Animate progress bar
                ObjectAnimator.ofInt(goalProgress, "progress", 0, performance).apply {
                    duration = 800
                    start()
                }

                if (total < minGoal || total > maxGoal) {
                    goalProgress.progressDrawable.setColorFilter(
                        ContextCompat.getColor(this@DashboardActivity, R.color.errorRed),
                        PorterDuff.Mode.SRC_IN
                    )
                    badgeText.text = "⚠️ Try Again Next Month"
                } else {
                    goalProgress.progressDrawable.setColorFilter(
                        ContextCompat.getColor(this@DashboardActivity, R.color.successGreen),
                        PorterDuff.Mode.SRC_IN
                    )
                    badgeText.text = "🏆 Budget Hero!"
                }
            }
        }
    }
}
