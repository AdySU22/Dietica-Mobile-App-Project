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
        val getStartedButton: Button = findViewById(R.id.btnLetsGetStarted)

        // Set the click listener
        getStartedButton.setOnClickListener {
            // Create an intent to start HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            // Optional: finish the current activity if you don't want to return to it
            finish()
        }
    }
}
