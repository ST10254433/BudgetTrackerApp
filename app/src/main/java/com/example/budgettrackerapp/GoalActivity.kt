package com.example.budgettrackerapp

import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class GoalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        val progressBar = findViewById<ProgressBar>(R.id.goalProgress)
        val spent = 180
        val minGoal = 100
        val maxGoal = 250

        val performance = ((spent - minGoal).toFloat() / (maxGoal - minGoal) * 100).toInt()

        // Animate progress bar
        ObjectAnimator.ofInt(progressBar, "progress", 0, performance).apply {
            duration = 800
            start()
        }

        if (spent < minGoal || spent > maxGoal) {
            progressBar.progressDrawable.setColorFilter(
                ContextCompat.getColor(this, R.color.errorRed),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            progressBar.progressDrawable.setColorFilter(
                ContextCompat.getColor(this, R.color.successGreen),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}
