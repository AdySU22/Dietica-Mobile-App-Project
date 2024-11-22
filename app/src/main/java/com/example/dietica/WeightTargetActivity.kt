package com.example.dietica

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException

class WeightTargetActivity : BaseActivity() {

    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weight_target)

        functions = FirebaseFunctions.getInstance()

        // Check if the user is authenticated
        val authId = FirebaseAuth.getInstance().currentUser?.uid
        if (authId == null) {
            Snackbar.make(findViewById(R.id.main), "User not authenticated. Please log in.", Snackbar.LENGTH_LONG).show()
            finish() // Close the activity if user is not authenticated
            return
        }

        // Retrieve UI elements
        val leftArrow2: ImageView = findViewById(R.id.leftArrow2)
        val saveButton: Button = findViewById(R.id.btnSave)
        val currentWeightInput: EditText = findViewById(R.id.currentWeightInput)
        val targetWeightInput: EditText = findViewById(R.id.targetWeightInput)
        val durationInput: EditText = findViewById(R.id.targetDurationInput)

        // Handle back navigation
        leftArrow2.setOnClickListener {
            finish()
        }

        // Handle save button click
        saveButton.setOnClickListener {
            val currentWeightText = currentWeightInput.text.toString().trim()
            val targetWeightText = targetWeightInput.text.toString().trim()
            val durationText = durationInput.text.toString().trim()

            // Log the input text to debug
            Log.d("WeightTargetActivity", "Current Weight Input: '$currentWeightText'")
            Log.d("WeightTargetActivity", "Target Weight Input: '$targetWeightText'")
            Log.d("WeightTargetActivity", "Duration Input: '$durationText'")

            // Check if any field is empty or still contains the hint
            if (currentWeightText.isEmpty() || currentWeightText == "Current Weight" ||
                targetWeightText.isEmpty() || targetWeightText == "Target Weight" ||
                durationText.isEmpty() || durationText == "Target Duration") {

                Snackbar.make(it, "Please fill in all fields with valid values.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Remove commas or spaces and handle invalid formatting
            val currentWeight = parseNumber(currentWeightText)
            val targetWeight = parseNumber(targetWeightText)
            val duration = durationText.toIntOrNull()

            // Log parsed values to debug
            Log.d("WeightTargetActivity", "Parsed Current Weight: $currentWeight")
            Log.d("WeightTargetActivity", "Parsed Target Weight: $targetWeight")
            Log.d("WeightTargetActivity", "Parsed Duration: $duration")

            if (currentWeight == null || targetWeight == null) {
                Snackbar.make(it, "Please enter valid numbers for weight.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (duration == null || duration <= 0) {
                Snackbar.make(it, "Please enter a valid duration (positive number).", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Save user target via Firebase Functions
            saveUserTarget(authId, currentWeight, targetWeight, duration)
        }
    }

    // Helper function to parse numbers, removing commas or spaces
    private fun parseNumber(numberString: String): Double? {
        // Log the raw input string before cleaning
        Log.d("WeightTargetActivity", "Parsing number: '$numberString'")

        return try {
            // Remove any commas or spaces from the number string
            val cleanedString = numberString.replace(",", "").replace(" ", "")
            // Log the cleaned string
            Log.d("WeightTargetActivity", "Cleaned number: '$cleanedString'")

            cleanedString.toDoubleOrNull()
        } catch (e: Exception) {
            // Log if there's an exception
            Log.e("WeightTargetActivity", "Error parsing number: $numberString", e)
            null
        }
    }

    private fun saveUserTarget(authId: String, currentWeight: Double, targetWeight: Double, duration: Int) {
        val data = hashMapOf(
            "authId" to authId,
            "currentWeight" to currentWeight,
            "targetWeight" to targetWeight,
            "duration" to duration
        )

        functions.getHttpsCallable("setUserTarget")
            .call(data)
            .addOnSuccessListener { result ->
                val message = (result.data as? Map<*, *>)?.get("message") as? String
                Log.d("WeightTargetActivity", "Success: $message")
                Snackbar.make(findViewById(R.id.main), message ?: "Target saved successfully!", Snackbar.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.e("WeightTargetActivity", "Error saving target", e)
                val errorDetails = (e as? FirebaseFunctionsException)?.details as? String
                Snackbar.make(findViewById(R.id.main), errorDetails ?: "An error occurred. Please try again.", Snackbar.LENGTH_LONG).show()
            }
    }
}
