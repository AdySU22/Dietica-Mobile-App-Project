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
        val date1: TextView = findViewById(R.id.date1)
        val date2: TextView = findViewById(R.id.date2)
        val date3: TextView = findViewById(R.id.date3)
        val dateFormat = SimpleDateFormat("dd\nMMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        btnBack.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
            startActivity(intent)
        }

        // Set the middle date (current date)
        date2.text = dateFormat.format(calendar.time)

        // Set previous day date
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        date1.text = dateFormat.format(calendar.time)

        // Set next day date
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        date3.text = dateFormat.format(calendar.time)
    }
}
