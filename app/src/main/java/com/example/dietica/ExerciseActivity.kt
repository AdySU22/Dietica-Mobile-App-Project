package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge

class ExerciseActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercise)

        val leftArrow: ImageView = findViewById(R.id.leftArrow)
        val exerciseNameEditText: EditText = findViewById(R.id.exerciseNameEditText)
        val hoursEditText: EditText = findViewById(R.id.hoursEditText)
        val minutesEditText: EditText = findViewById(R.id.minutesEditText)
        val secondsEditText: EditText = findViewById(R.id.secondsEditText)
        val btnCancel: Button = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        leftArrow.setOnClickListener {
            val intent = Intent(this,MyExerciseActivity::class.java)
            startActivity(intent)
        }

        btnCancel.setOnClickListener {
            val intent = Intent(this,MyExerciseActivity::class.java)
            startActivity(intent)
        }


    }
}