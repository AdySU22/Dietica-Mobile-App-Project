package com.example.dietica

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class ExerciseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercise)

        val leftArrow: ImageView = findViewById(R.id.leftArrow)
        val plusButton: ImageView = findViewById(R.id.plusButton)

        leftArrow.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        plusButton.setOnClickListener {
            val intent = Intent(this, TreadmillActivity::class.java)
            startActivity(intent)
        }
    }
}