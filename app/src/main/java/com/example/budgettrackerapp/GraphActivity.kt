package com.example.budgettrackerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.components.LimitLine

class GraphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        val chart = findViewById<BarChart>(R.id.categoryChart)
        val entries = listOf(
            BarEntry(0f, 120f), // Food
            BarEntry(1f, 80f),  // Transport
            BarEntry(2f, 200f)  // Rent
        )

        val dataSet = BarDataSet(entries, "Expenses per Category")
        dataSet.color = ContextCompat.getColor(this, R.color.colorPrimary)

        val data = BarData(dataSet)
        chart.data = data

        val minGoal = LimitLine(100f, "Min Goal")
        val maxGoal = LimitLine(250f, "Max Goal")
        chart.axisLeft.addLimitLine(minGoal)
        chart.axisLeft.addLimitLine(maxGoal)

        // Animate chart
        chart.animateY(1000)

        chart.invalidate()
    }
}
