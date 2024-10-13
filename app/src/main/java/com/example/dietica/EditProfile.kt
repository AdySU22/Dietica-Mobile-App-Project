package com.example.dietica

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EditProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Optional: Enable immersive display
        setContentView(R.layout.activity_edit_profile)

        // Find views by ID
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)
        val textContainer: LinearLayout = findViewById(R.id.textContainer)
        val headerText: TextView = findViewById(R.id.headerText)
        val subheaderText: TextView = findViewById(R.id.subheaderText)

        val iconClickListener = View.OnClickListener { view ->
            textContainer.visibility = View.VISIBLE

            when (view.id) {
                R.id.icon1 -> {
                    headerText.text = "Sedentary"
                    subheaderText.text = "Typical daily activities. Measures basal metabolic rate."
                }
                R.id.icon2 -> {
                    headerText.text = "Light Activity"
                    subheaderText.text = "Typical daily activity with 30–60 minutes of exercise. E.g., walking 5km."
                }
                R.id.icon3 -> {
                    headerText.text = "Active"
                    subheaderText.text = "Typical daily activity with at least 60 minutes of exercise."
                }
                R.id.icon4 -> {
                    headerText.text = "Very Active"
                    subheaderText.text = "Typical daily activity with at least 60 minutes of heavy exercise."
                }
            }
        }

        // Set click listeners for all icons
        icon1.setOnClickListener(iconClickListener)
        icon2.setOnClickListener(iconClickListener)
        icon3.setOnClickListener(iconClickListener)
        icon4.setOnClickListener(iconClickListener)
    }
}
