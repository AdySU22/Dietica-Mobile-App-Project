package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.TimeUtils.formatDuration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class MyExerciseActivity : BaseActivity() {

    private lateinit var exerciseContainer: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_exercise)

        val btnBack = findViewById<ImageView>(R.id.leftArrow)
        btnBack.setOnClickListener {
            finish()
        }

        val btnPlus = findViewById<ImageView>(R.id.btnPlus)
        btnPlus.setOnClickListener {
            // Navigate to a new activity to add exercise data
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }

        exerciseContainer = findViewById(R.id.exerciseContainer)

        // Fetch Exercise Logs for Today's Activity
        fetchExerciseLogs()
    }

    private fun fetchExerciseLogs() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            println("Error: User not authenticated")
            showEmptyState()
            return
        }

        val authId = user.uid
        val currentDate = getCurrentDate()

        println("Fetching exercises for authId=$authId and date=$currentDate")

        db.collection("ExerciseLogs")
            .whereEqualTo("authId", authId)
            .whereEqualTo("date", currentDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                println("Query successful. Documents found: ${querySnapshot.size()}")
                querySnapshot.documents.forEach { doc ->
                    println("Document data: ${doc.data}")
                }
                if (querySnapshot.isEmpty) {
                    showEmptyState()
                } else {
                    populateExercises(querySnapshot)
                }
            }
            .addOnFailureListener { e ->
                println("Query failed: ${e.message}")
                showErrorState(e.message)
            }
    }

    private fun populateExercises(querySnapshot: QuerySnapshot) {
        exerciseContainer.removeAllViews() // Clear existing views

        for (document in querySnapshot.documents) {
            val name = document.getString("name") ?: "Exercise Name"
            val duration = document.getLong("duration") ?: 0L

            // Format duration as "X hours, Y minutes, Z seconds"
            val formattedDuration = formatDuration(duration)

            // Inflate exercise layout dynamically
            val exerciseView = LayoutInflater.from(this).inflate(R.layout.item_exercise, exerciseContainer, false)

            // Bind data
            exerciseView.findViewById<TextView>(R.id.textView7).text = name
            exerciseView.findViewById<TextView>(R.id.textView8).text = "Duration: $formattedDuration"

            // Add the exercise view to the container
            exerciseContainer.addView(exerciseView)
        }
    }

    private fun formatDuration(durationInSeconds: Long): String {
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val seconds = durationInSeconds % 60

        val parts = mutableListOf<String>()
        if (hours > 0) parts.add("$hours hours")
        if (minutes > 0) parts.add("$minutes minutes")
        if (seconds > 0) parts.add("$seconds seconds")

        return parts.joinToString(", ")
    }

    private fun showEmptyState() {
        exerciseContainer.removeAllViews()
        val emptyMessage = TextView(this)
        emptyMessage.text = "No activities found for today."
        emptyMessage.textSize = 16f
        emptyMessage.setTextColor(getColor(R.color.black))
        exerciseContainer.addView(emptyMessage)
    }

    private fun showErrorState(message: String?) {
        // Display an error message
        val errorMessage = TextView(this)
        errorMessage.text = "Error fetching data: $message"
        errorMessage.textSize = 16f
        errorMessage.setTextColor(getColor(R.color.black))
        exerciseContainer.addView(errorMessage)
    }

    private fun getCurrentDate(): String {
        // Ensure this format matches Firestore's date field
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }
}
