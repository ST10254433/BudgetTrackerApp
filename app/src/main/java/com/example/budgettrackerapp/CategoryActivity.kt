package com.example.budgettrackerapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Toolbar with back arrow
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getDatabase(this)

        val categoryField = findViewById<EditText>(R.id.categoryField)
        val saveButton = findViewById<Button>(R.id.saveCategoryButton)

        saveButton.setOnClickListener {
            val name = categoryField.text.toString()
            if (name.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    db.categoryDao().insertCategory(Category(name = name))
                    runOnUiThread {
                        Toast.makeText(this@CategoryActivity, "Category saved!", Toast.LENGTH_SHORT).show()
                        categoryField.text.clear()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }
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
