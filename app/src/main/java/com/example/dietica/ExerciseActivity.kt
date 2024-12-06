package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

class ExerciseActivity : BaseActivity() {
    private lateinit var functions: FirebaseFunctions
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercise)

        val leftArrow: ImageView = findViewById(R.id.leftArrow)
        val exerciseNameEditText: EditText = findViewById(R.id.exerciseNameEditText)
        val hoursEditText: EditText = findViewById(R.id.hoursEditText)
        val minutesEditText: EditText = findViewById(R.id.minutesEditText)
        val secondsEditText: EditText = findViewById(R.id.secondsEditText)
        val btnCancel: Button = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        // Initialize Firebase instances
        functions = FirebaseFunctions.getInstance()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Navigate to MyExerciseActivity on Cancel or Back
        leftArrow.setOnClickListener { navigateToMyExerciseActivity() }
        btnCancel.setOnClickListener { navigateToMyExerciseActivity() }

        // Save button click listener
        btnSave.setOnClickListener {
            val exerciseName = exerciseNameEditText.text.toString().trim()
            val hours = hoursEditText.text.toString().toIntOrNull() ?: 0
            val minutes = minutesEditText.text.toString().toIntOrNull() ?: 0
            val seconds = secondsEditText.text.toString().toIntOrNull() ?: 0

            if (exerciseName.isEmpty()) {
                Toast.makeText(this, "Please enter an exercise name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val duration = calculateDuration(hours, minutes, seconds)
            if (duration <= 0) {
                Toast.makeText(this, "Please enter a valid duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveExerciseLog(exerciseName, duration)
        }
    }

    private fun calculateDuration(hours: Int, minutes: Int, seconds: Int): Int {
        return hours * 3600 + minutes * 60 + seconds
    }

    private fun saveExerciseLog(name: String, duration: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val authId = user.uid
        val date = getCurrentDate()

        val exerciseData = hashMapOf(
            "authId" to authId,
            "name" to name,
            "duration" to duration,
            "date" to date,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("ExerciseLogs")
            .add(exerciseData)
            .addOnSuccessListener {
                Toast.makeText(this, "Exercise saved successfully", Toast.LENGTH_SHORT).show()
                navigateToMyExerciseActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save exercise log: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getCurrentDate(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    private fun navigateToMyExerciseActivity() {
        val intent = Intent(this, MyExerciseActivity::class.java)
        startActivity(intent)
    }
}
