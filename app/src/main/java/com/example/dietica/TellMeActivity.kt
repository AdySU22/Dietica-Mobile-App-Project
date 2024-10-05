package com.example.dietica

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TellMeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tell_me)

        val genderSpinner: Spinner = findViewById(R.id.genderInput)
        val activitySpinner: Spinner = findViewById(R.id.activityLevelInput)

        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.activity_level_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            activitySpinner.adapter = adapter
        }
    }
}
