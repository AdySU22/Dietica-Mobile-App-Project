package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MyExerciseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_exercise)

        val leftArrow: ImageView = findViewById(R.id.leftArrow)
        val btnPlus: ImageView = findViewById(R.id.btnPlus)

        leftArrow.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        /*btnPlus.setOnClickListener {
            val intent = Intent(this, ::class.java)
            startActivity(intent)
        }*/
    }
}