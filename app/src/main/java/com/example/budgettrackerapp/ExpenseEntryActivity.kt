package com.example.budgettrackerapp

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseEntryActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var goalSeekBar: SeekBar
    private lateinit var goalLabel: TextView
    private lateinit var photoPreview: ImageView
    private lateinit var addPhotoButton: Button
    private var selectedPhotoUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)

        // Toolbar with back arrow
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getDatabase(this)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val descriptionField = findViewById<EditText>(R.id.descriptionField)
        val amountField = findViewById<EditText>(R.id.amountField)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        goalSeekBar = findViewById(R.id.goalSeekBar)
        goalLabel = findViewById(R.id.goalLabel)
        photoPreview = findViewById(R.id.photoPreview)
        addPhotoButton = findViewById(R.id.addPhotoButton)

        // Image picker
        val pickImage = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedPhotoUri = uri.toString()
                photoPreview.visibility = ImageView.VISIBLE
                photoPreview.setImageURI(uri)
            }
        }

        addPhotoButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Load categories
        CoroutineScope(Dispatchers.IO).launch {
            val categories = db.categoryDao().getAllCategories()
            val names = categories.map { it.name }
            runOnUiThread {
                val adapter = ArrayAdapter(
                    this@ExpenseEntryActivity,
                    android.R.layout.simple_spinner_item,
                    names
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
        }

        // Load existing goal
        val currentGoal = getGoal()
        goalSeekBar.progress = currentGoal
        goalLabel.text = "Monthly Goal: R$currentGoal"

        goalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                goalLabel.text = "Monthly Goal: R$progress"
                saveGoals(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            val desc = descriptionField.text.toString()
            val amtText = amountField.text.toString()
            val amt = amtText.toDoubleOrNull()
            val cat = categorySpinner.selectedItem?.toString() ?: ""
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (desc.isNotBlank() && amt != null && cat.isNotBlank()) {
                val expense = Expense(
                    description = desc,
                    amount = amt,
                    categoryName = cat,
                    date = date,
                    startTime = "08:00",
                    endTime = "09:00",
                    photoUri = selectedPhotoUri
                )

                CoroutineScope(Dispatchers.IO).launch {
                    db.expenseDao().insertExpense(expense)
                    val total = db.expenseDao().getTotalExpenses() ?: 0.0
                    val maxGoal = getGoal()

                    runOnUiThread {
                        if (total > maxGoal) {
                            Toast.makeText(this@ExpenseEntryActivity, "⚠️ You exceeded your monthly goal!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@ExpenseEntryActivity, "Expense saved! Total: R$total", Toast.LENGTH_SHORT).show()
                        }
                        descriptionField.text.clear()
                        amountField.text.clear()
                        photoPreview.visibility = ImageView.GONE
                        selectedPhotoUri = null
                    }
                }
            } else {
                Toast.makeText(this, "Enter valid details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveGoals(max: Int) {
        val prefs = getSharedPreferences("budget_prefs", MODE_PRIVATE)
        prefs.edit().putInt("maxGoal", max).apply()
    }

    private fun getGoal(): Int {
        val prefs = getSharedPreferences("budget_prefs", MODE_PRIVATE)
        return prefs.getInt("maxGoal", 10000)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

}
