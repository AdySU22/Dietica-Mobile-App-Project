package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class InputMealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_input_meal)

        val btnBack: Button = findViewById(R.id.btnBack)
        val btnManualInput: Button = findViewById(R.id.btnManualInput)

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        btnManualInput.setOnClickListener {
            val intent = Intent(this, ManuallyInputMeal::class.java)
            startActivity(intent)
        }

    }
}