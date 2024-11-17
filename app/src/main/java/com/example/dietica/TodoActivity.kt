package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoActivity : AppCompatActivity() {
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        functions = FirebaseFunctions.getInstance()

        // Decide whether to use the emulator
        if (false) {
            // Replace "10.0.2.2" with "localhost" if testing on a physical device
            functions.useEmulator("10.0.2.2", 5001)
            Log.d("FirebaseEmulator", "Using Firebase Emulator for Functions")
        }

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val date1Day: TextView = findViewById(R.id.date1_day)
        val date1Month: TextView = findViewById(R.id.date1_month)
        val date2Day: TextView = findViewById(R.id.date2_day)
        val date2Month: TextView = findViewById(R.id.date2_month)
        val date3Day: TextView = findViewById(R.id.date3_day)
        val date3Month: TextView = findViewById(R.id.date3_month)

        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val btnGo: Button = findViewById(R.id.btnGo)

        btnBack.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
            startActivity(intent)
        }

        btnGo.setOnClickListener {
            val intent = Intent(this, AIChatBot::class.java)
            startActivity(intent)
        }

        // Set the middle date (current date)
        date2Day.text = dayFormat.format(calendar.time)
        date2Month.text = monthFormat.format(calendar.time)

        // Set previous day date
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        date1Day.text = dayFormat.format(calendar.time)
        date1Month.text = monthFormat.format(calendar.time)

        // Set next day date
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        date3Day.text = dayFormat.format(calendar.time)
        date3Month.text = monthFormat.format(calendar.time)

        initTodo()
    }

    private fun initTodo() {
        val foodTitle = findViewById<TextView>(R.id.foodTitle)
        val foodDescription = findViewById<TextView>(R.id.foodDescription)
        val exerciseTitle = findViewById<TextView>(R.id.exerciseTitle)
        val exerciseDescription = findViewById<TextView>(R.id.exerciseDescription)
        val waterTitle = findViewById<TextView>(R.id.waterTitle)
        val waterDescription = findViewById<TextView>(R.id.waterDescription)

        lifecycleScope.launch {
            try {
                val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                val authId = sharedPreferences.getString("authId", null)
                val data = mapOf("authId" to authId)
                val result = functions
                    .getHttpsCallable("getTodoV2")
                    .call(data)
                    .await()

                val resultData = result.data as? Map<*, *> ?: return@launch

                foodTitle.text = resultData["foodTitle"]?.toString() ?: ""
                foodDescription.text = resultData["foodDescription"]?.toString() ?: ""
                exerciseTitle.text = resultData["exerciseTitle"]?.toString() ?: ""
                exerciseDescription.text = resultData["exerciseDescription"]?.toString() ?: ""
                waterTitle.text = resultData["waterTitle"]?.toString() ?: ""
                waterDescription.text = resultData["waterDescription"]?.toString() ?: ""
            } catch (e: Exception) {
                Log.e("TodoActivity", "Error fetching data", e)
                Toast.makeText(this@TodoActivity, "Failed to get Todo List", Toast.LENGTH_SHORT).show()

                foodTitle.text = "Food"
                foodDescription.text = "Failed to get recommendation"
                exerciseTitle.text = "Exercise"
                exerciseDescription.text = "Failed to get recommendation"
                waterTitle.text = "Water"
                waterDescription.text = "Failed to get recommendation"
            }
        }
    }
}
