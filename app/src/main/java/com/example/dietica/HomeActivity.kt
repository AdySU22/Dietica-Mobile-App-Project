package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout

class HomeActivity : AppCompatActivity() {
    private lateinit var todayTotalTextView: TextView
    private lateinit var inputMealTextView: TextView
    private lateinit var profileButton: Button
    private lateinit var btnNotification: ImageView
    private lateinit var btnGo: Button
    private lateinit var exercisePlus: ImageView
    private lateinit var weightPlus: ImageView
    private lateinit var buttonBodyComposition : Button
    private lateinit var homeLinearLayout: LinearLayout
    private lateinit var exerciseFrameLayout: FrameLayout
    private lateinit var reportLinearLayout: LinearLayout
    private lateinit var toDoFrameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
        val authId = sharedPreferences.getString("authId", null)
        Log.d("HomeActivity", authId ?: "Token is null")

        // Setting up the UI elements
        todayTotalTextView = findViewById(R.id.today_total)
        inputMealTextView = findViewById(R.id.inputMealText)
        profileButton = findViewById(R.id.profileButton)
        btnNotification = findViewById(R.id.btnNotification)
        btnGo = findViewById(R.id.btnGo)
        exercisePlus = findViewById(R.id.exercisePlus)
        weightPlus = findViewById(R.id.weightPlus)
        buttonBodyComposition = findViewById(R.id.buttonBodyComposition)
        homeLinearLayout = findViewById(R.id.homeLinearLayout)
        exerciseFrameLayout = findViewById(R.id.exerciseFrameLayout)
        reportLinearLayout = findViewById(R.id.reportLinearLayout)
        toDoFrameLayout = findViewById(R.id.toDoFrameLayout)

        // Example: Update today's total dynamically
        updateTodayTotal(2500)  // Set your desired total

        inputMealTextView.setOnClickListener {
            val intent = Intent(this, InputMealActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }

        btnNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        btnGo.setOnClickListener {
            val intent = Intent(this, HealthReportActivity::class.java)
            startActivity(intent)
        }

        exercisePlus.setOnClickListener {
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }

        weightPlus.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
            startActivity(intent)
        }

        buttonBodyComposition.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
            startActivity(intent)
        }

        homeLinearLayout.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        exerciseFrameLayout.setOnClickListener {
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }

        reportLinearLayout.setOnClickListener {
            val intent = Intent(this, HealthReportActivity::class.java)
            startActivity(intent)
        }

        toDoFrameLayout.setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateTodayTotal(total: Int) {
        todayTotalTextView.text = total.toString()
    }
}
