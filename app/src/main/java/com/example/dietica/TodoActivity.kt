package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

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

        // Set back button functionality
        btnBack.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
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
    }
}
