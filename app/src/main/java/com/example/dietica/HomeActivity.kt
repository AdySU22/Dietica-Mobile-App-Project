package com.example.dietica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout

class HomeActivity : AppCompatActivity() {
    private lateinit var todayTotalTextView: TextView
    private lateinit var inputMealTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var notificationIconImageView: ImageView
    private lateinit var homeLinearLayout: LinearLayout
    private lateinit var reportLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Setting up the UI elements
        todayTotalTextView = findViewById(R.id.today_total)
        inputMealTextView = findViewById(R.id.inputMealText)
        profileImageView = findViewById(R.id.profile_image)
        notificationIconImageView = findViewById(R.id.notification_icon)
        homeLinearLayout = findViewById(R.id.homeLinearLayout)
        reportLinearLayout = findViewById(R.id.reportLinearLayout)

        // Example: Update today's total dynamically
        updateTodayTotal(2500)  // Set your desired total

        homeLinearLayout.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        reportLinearLayout.setOnClickListener {
            val intent = Intent(this, HealthReportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateTodayTotal(total: Int) {
        todayTotalTextView.text = total.toString()
    }
}
