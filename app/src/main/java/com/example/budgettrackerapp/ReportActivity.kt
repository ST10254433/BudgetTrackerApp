package com.example.budgettrackerapp

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var exportButton: Button
    private lateinit var shareButton: Button
    private var pdfFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        db = AppDatabase.getDatabase(this)
        exportButton = findViewById(R.id.exportPdfButton)
        shareButton = findViewById(R.id.sharePdfButton)

        // Animate buttons on load
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        exportButton.startAnimation(fadeIn)
        shareButton.startAnimation(fadeIn)

        exportButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val expenses = db.expenseDao().getAllExpenses()
                val total = db.expenseDao().getTotalExpenses() ?: 0.0
                val prefs = getSharedPreferences("budget_prefs", MODE_PRIVATE)
                val maxGoal = prefs.getInt("maxGoal", 10000)
                val minGoal = prefs.getInt("minGoal", 0)

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.format(Date())

                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(600, 800, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()

                paint.textSize = 18f
                canvas.drawText("BudgetTracker Monthly Report", 50f, 50f, paint)

                paint.textSize = 14f
                canvas.drawText("Date: $today", 50f, 80f, paint)
                canvas.drawText("Total Expenses: R$total", 50f, 110f, paint)
                canvas.drawText("Goal Range: R$minGoal - R$maxGoal", 50f, 140f, paint)

                var yPos = 180f
                for (expense in expenses) {
                    canvas.drawText("${expense.date} | ${expense.categoryName} | R${expense.amount}", 50f, yPos, paint)
                    yPos += 20f
                }

                pdfDocument.finishPage(page)

                pdfFile = File(filesDir, "MonthlyReport.pdf")
                pdfDocument.writeTo(FileOutputStream(pdfFile!!))
                pdfDocument.close()

                runOnUiThread {
                    Toast.makeText(this@ReportActivity, "PDF exported to ${pdfFile!!.absolutePath}", Toast.LENGTH_LONG).show()
                }
            }
        }

        shareButton.setOnClickListener {
            if (pdfFile != null && pdfFile!!.exists()) {
                val uri: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    pdfFile!!
                )
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/pdf"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
            } else {
                Toast.makeText(this, "Export the PDF first!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
