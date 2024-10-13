package com.example.dietica

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EditProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Optional: Enable immersive display
        setContentView(R.layout.activity_edit_profile)

        // Find views by ID for activity level icons
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)
        val textContainer: LinearLayout = findViewById(R.id.textContainer)
        val headerText: TextView = findViewById(R.id.headerText)
        val subheaderText: TextView = findViewById(R.id.subheaderText)

        // Find views for personal information sections
        val genderText: TextView = findViewById(R.id.genderText)
        val genderIcon: ImageView = findViewById(R.id.genderIcon)
        val heightText: TextView = findViewById(R.id.heightText)
        val heightIcon: ImageView = findViewById(R.id.heightIcon)
        val weightText: TextView = findViewById(R.id.weightText)
        val weightIcon: ImageView = findViewById(R.id.weightIcon)
        val birthDateText: TextView = findViewById(R.id.birtDateText)
        val birthDateIcon: ImageView = findViewById(R.id.birtDateIcon)
        val guestUserText: TextView = findViewById(R.id.guestUserText) // Guest User TextView

        // Set click listener for Guest User TextView to edit the name
        guestUserText.setOnClickListener {
            showNameEditDialog(guestUserText)
        }

        // Activity level click listener
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

        // Set click listeners for all activity level icons
        icon1.setOnClickListener(iconClickListener)
        icon2.setOnClickListener(iconClickListener)
        icon3.setOnClickListener(iconClickListener)
        icon4.setOnClickListener(iconClickListener)

        // Set click listeners for personal information sections
        genderText.setOnClickListener { showGenderPicker(genderText) }
        genderIcon.setOnClickListener { showGenderPicker(genderText) }

        heightText.setOnClickListener { showNumberPicker(heightText, "Select Height", 100, 250) }
        heightIcon.setOnClickListener { showNumberPicker(heightText, "Select Height", 100, 250) }

        weightText.setOnClickListener { showNumberPicker(weightText, "Select Weight", 30, 150) }
        weightIcon.setOnClickListener { showNumberPicker(weightText, "Select Weight", 30, 150) }

        birthDateText.setOnClickListener { showDatePicker(birthDateText) }
        birthDateIcon.setOnClickListener { showDatePicker(birthDateText) }
    }

    // Function to show a dialog with EditText to edit the name
    private fun showNameEditDialog(textView: TextView) {
        val editText = EditText(this).apply {
            setText(textView.text)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                textView.text = editText.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to show a number picker dialog
    private fun showNumberPicker(textView: TextView, title: String, minValue: Int, maxValue: Int) {
        val numberPicker = NumberPicker(this).apply {
            this.minValue = minValue
            this.maxValue = maxValue
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                val value = numberPicker.value
                val unit = if (textView.id == R.id.heightText) " cm" else " kg"
                textView.text = "$value$unit"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to show a gender picker dialog
    private fun showGenderPicker(textView: TextView) {
        val genders = arrayOf("Male", "Female")
        AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(genders) { _, which ->
                textView.text = genders[which]
            }
            .show()
    }

    // Function to show a date picker dialog
    private fun showDatePicker(textView: TextView) {
        val datePicker = DatePicker(this)
        AlertDialog.Builder(this)
            .setTitle("Select Birth Date")
            .setView(datePicker)
            .setPositiveButton("OK") { _, _ ->
                val day = datePicker.dayOfMonth
                val month = datePicker.month + 1
                val year = datePicker.year
                textView.text = String.format("%02d-%02d-%d", day, month, year)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
