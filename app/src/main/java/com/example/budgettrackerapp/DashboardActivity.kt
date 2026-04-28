package com.example.budgettrackerapp

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var totalExpensesLabel: TextView
    private lateinit var goalLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable shared element transition for logo morph
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)

        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(findViewById(R.id.toolbar)) // root, no back arrow

        db = AppDatabase.getDatabase(this)

        totalExpensesLabel = findViewById(R.id.totalExpensesLabel)
        goalLabel = findViewById(R.id.goalLabel)

        val addExpenseButton = findViewById<Button>(R.id.addExpenseButton)
        val viewExpensesButton = findViewById<Button>(R.id.viewExpensesButton)
        val addCategoryButton = findViewById<Button>(R.id.addCategoryButton)

        addExpenseButton.setOnClickListener {
            startActivity(Intent(this, ExpenseEntryActivity::class.java))
        }

        viewExpensesButton.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        addCategoryButton.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
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

            runOnUiThread {
                totalExpensesLabel.text = "Total Expenses: R$total"
                goalLabel.text = "Monthly Goal: R$maxGoal"
            }
        }
    }
}
