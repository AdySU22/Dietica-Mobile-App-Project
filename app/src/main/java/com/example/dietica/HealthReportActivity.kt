package com.example.dietica

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HealthReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_health_report)

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val graphButton: Button = findViewById(R.id.graphButton)
        graphButton.setOnClickListener {
            // Action to show more detailed graph
            // Not yet implemented
        }

        val weeklyText: TextView = findViewById(R.id.weeklyText)
        weeklyText.visibility = View.VISIBLE
    }
}