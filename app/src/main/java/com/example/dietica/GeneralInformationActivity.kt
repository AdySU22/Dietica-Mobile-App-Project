package com.example.dietica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class GeneralInformationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_general_information)

        // Initialize the button
        val btnNext: Button = findViewById(R.id.btnNext)

        // Set the click listener
        btnNext.setOnClickListener {
            val intent = Intent(this, TellMeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
