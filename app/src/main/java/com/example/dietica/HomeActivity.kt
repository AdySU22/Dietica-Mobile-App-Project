package com.example.dietica

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

class HomeActivity : AppCompatActivity() {
    private lateinit var todayTotalTextView: TextView
    private lateinit var inputMealTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var notificationIconImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Setting up the UI elements
        todayTotalTextView = findViewById(R.id.today_total)
        inputMealTextView = findViewById(R.id.inputMealText)
        profileImageView = findViewById(R.id.profile_image)
        notificationIconImageView = findViewById(R.id.notification_icon)

        // Example: Update today's total dynamically
        updateTodayTotal(2500)  // Set your desired total
    }

    private fun updateTodayTotal(total: Int) {
        todayTotalTextView.text = total.toString()
    }
}