package com.example.dietica

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class HomeActivity : AppCompatActivity() {
    private lateinit var waterCountTextView: TextView
    private lateinit var inputMealTextView: TextView
    private lateinit var profileButton: Button
    private lateinit var btnNotification: ImageView
    private lateinit var btnGo: Button
    private lateinit var exercisePlus: ImageView
    private lateinit var weightPlus: ImageView
    private lateinit var buttonBodyComposition: Button
    private lateinit var homeLinearLayout: LinearLayout
    private lateinit var exerciseFrameLayout: FrameLayout
    private lateinit var reportLinearLayout: LinearLayout
    private lateinit var toDoFrameLayout: FrameLayout
    private lateinit var addWaterText: EditText
    private lateinit var functions: FirebaseFunctions
    private lateinit var sharedPreferences: SharedPreferences
    private var totalWaterIntake = 0 // Store total water intake in ml
    private val dailyWaterGoal = 2000 // Daily water goal in ml

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        applyImmersiveMode()

        // Initialize Firebase Functions
        functions = Firebase.functions

        sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
        val authId = sharedPreferences.getString("authId", null)
        Log.d("HomeActivity", authId ?: "Token is null")

        // Check if the water intake needs to be reset
        resetWaterIntakeIfNewDay()

        // Setting up the UI elements
        waterCountTextView = findViewById(R.id.waterCount)
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
        addWaterText = findViewById(R.id.addWaterText)

        // Load saved water intake amount
        totalWaterIntake = sharedPreferences.getInt("totalWaterIntake", 0)
        updateWaterCount(totalWaterIntake)

        addWaterText.setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter amount in ml"

            AlertDialog.Builder(this)
                .setTitle("Log Water Intake")
                .setMessage("Enter the amount of water (in ml):")
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    val amount = input.text.toString().toIntOrNull()
                    if (amount != null && amount > 0) {
                        logWaterIntake(amount)
                    } else {
                        Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        inputMealTextView.setOnClickListener {
            val intent = Intent(this, InputMealActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val authId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("HomeActivity", "Auth ID: $authId")  // Check the value of authId
            val intent = Intent(this, ProfilePageActivity::class.java)
            if (authId != null) {
                intent.putExtra("authId", authId)
                Log.d("HomeActivity", "Passing Auth ID: $authId")  // Log the passed authId
            } else {
                Log.d("HomeActivity", "User not authenticated")
            }
            startActivity(intent)
        }

        btnNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        btnGo.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
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

    override fun onResume() {
        super.onResume()
        applyImmersiveMode()
    }

    private fun applyImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    private fun resetWaterIntakeIfNewDay() {
        // Get the current date
        val currentDate = System.currentTimeMillis()
        val lastResetTimestamp = sharedPreferences.getLong("lastResetTimestamp", 0)

        // If last reset timestamp is not from today, reset the water intake
        if (lastResetTimestamp != getCurrentDayTimestamp(currentDate)) {
            totalWaterIntake = 0
            sharedPreferences.edit().putInt("totalWaterIntake", totalWaterIntake).apply()
            sharedPreferences.edit()
                .putLong("lastResetTimestamp", getCurrentDayTimestamp(currentDate)).apply()
        }
    }

    private fun getCurrentDayTimestamp(currentTimeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun logWaterIntake(amount: Int) {
        val authId = sharedPreferences.getString("authId", null)

        if (authId != null) {
            val data = hashMapOf(
                "authId" to authId,
                "amount" to amount
            )

            functions
                .getHttpsCallable("setWaterLog")
                .call(data)
                .addOnSuccessListener { result ->
                    val message = result.data as Map<*, *>
                    Log.d("HomeActivity", "Water log created: ${message["message"]}")

                    // Update the local total and UI for water intake
                    totalWaterIntake += amount
                    updateWaterCount(totalWaterIntake)

                    // Save the updated totalWaterIntake to SharedPreferences
                    saveWaterIntake(totalWaterIntake)
                }
                .addOnFailureListener { e ->
                    Log.e("HomeActivity", "Failed to log water intake", e)
                }
        } else {
            Log.e("HomeActivity", "authId is null")
        }
    }

    private fun updateWaterCount(total: Int) {
        // Update water intake text in the format "current/goal ml"
        waterCountTextView.text = "$total/$dailyWaterGoal ml"
    }

    private fun saveWaterIntake(total: Int) {
        // Save total water intake to SharedPreferences
        sharedPreferences.edit().putInt("totalWaterIntake", total).apply()
    }
}
