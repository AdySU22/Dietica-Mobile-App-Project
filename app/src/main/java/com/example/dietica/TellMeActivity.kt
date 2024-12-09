package com.example.dietica

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.widget.addTextChangedListener
import com.example.dietica.services.LoadingUtils
import com.google.firebase.functions.FirebaseFunctions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TellMeActivity : BaseActivity() {

    private lateinit var dobInput: TextView
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var chronicIllnessInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var btnLetsGetStarted: Button
    private lateinit var firebaseFunctions: FirebaseFunctions

    private lateinit var progressOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tell_me)

        // Initialize Views
        dobInput = findViewById(R.id.dobInput)
        heightInput = findViewById(R.id.heightInput)
        weightInput = findViewById(R.id.weightInput)
        chronicIllnessInput = findViewById(R.id.chronicIllnessInput)
        genderSpinner = findViewById(R.id.genderInput)
        activityLevelSpinner = findViewById(R.id.activityLevelInput)
        btnLetsGetStarted = findViewById(R.id.btnLetsGetStarted)

        // Initialize Firebase Functions
        firebaseFunctions = FirebaseFunctions.getInstance()

        // Initialize loading
        progressOverlay = findViewById(R.id.progress_overlay)

        // Set up Adapters for Spinners
        setupSpinnerAdapter(genderSpinner, R.array.gender_options)
        setupSpinnerAdapter(activityLevelSpinner, R.array.activity_level_options)

        // Get the authId from Intent or SharedPreferences
        val authId = intent.getStringExtra("authId")
        if (authId != null) {
            // Log the received authId
            Log.d("TellMeActivity", "Received authId: $authId")
            setupListeners(authId)
        } else {
            // Handle the case where authId is missing
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Validate inputs initially to enable button
        validateInputs()

        // Add listeners to inputs for real-time validation
        dobInput.addTextChangedListener { validateInputs() }
        heightInput.addTextChangedListener { validateInputs() }
        weightInput.addTextChangedListener { validateInputs() }
        chronicIllnessInput.addTextChangedListener { validateInputs() }
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                validateInputs()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        activityLevelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                validateInputs()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Override onClick listener
        dobInput.setOnClickListener {
            showDatePicker(dobInput)
        }
    }

    private fun setupListeners(authId: String) {
        // Add listeners and validation logic for button click
        btnLetsGetStarted.setOnClickListener {
            val email = intent.getStringExtra("email")
            val firstName = intent.getStringExtra("firstName")
            val lastName = intent.getStringExtra("lastName")
            val birthdate = (dobInput.tag as? Date)?.let { date ->
                // Convert Date to ISO string
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(date)
            }
            val data = hashMapOf(
                "authId" to authId,
                "email" to email,
                "firstName" to firstName,
                "lastName" to lastName,
                "weight" to weightInput.text.toString().toDoubleOrNull(),
                "height" to heightInput.text.toString().toDoubleOrNull(),
                "gender" to genderSpinner.selectedItem.toString(),
                "activityLevels" to activityLevelSpinner.selectedItem.toString(),
                "illnesses" to chronicIllnessInput.text.toString().takeIf { it.isNotEmpty() },
                "birthdate" to birthdate
            )

            Log.d("TellMeActivity", "Data to be sent to Firebase: $data")

            // Start loading overlay
            LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)

            // Call the Firebase function
            firebaseFunctions
                .getHttpsCallable("setProfileV2")
                .call(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Welcome to Dietica!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                    } else {
                        Log.e("TellMeActivity", "Firebase function call failed", task.exception)
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                    }
                    // Hide loading overlay
                    LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)
                }
        }
    }

    // Set up Spinner Adapter
    private fun setupSpinnerAdapter(spinner: Spinner, arrayResId: Int) {
        ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    // Validate inputs and enable button if valid
    private fun validateInputs() {
        val ageNotEmpty = dobInput.text.isNotEmpty()
        val heightNotEmpty = heightInput.text.toString().toDoubleOrNull() != null
        val weightNotEmpty = weightInput.text.toString().toDoubleOrNull() != null
        val chronicIllnessNotEmpty = chronicIllnessInput.text.isNotEmpty()

        val genderSelected = genderSpinner.selectedItemPosition != 0  // 0 means "Select Gender"
        val activitySelected = activityLevelSpinner.selectedItemPosition != 0

        val isButtonEnabled = ageNotEmpty && heightNotEmpty && weightNotEmpty &&
                chronicIllnessNotEmpty && genderSelected && activitySelected

        btnLetsGetStarted.isEnabled = isButtonEnabled
    }

    private fun showDatePicker(ageInput: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format as "Date Month Year"
                val formattedDate = String.format(
                    "%02d %s %d",
                    selectedDay,
                    calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()),
                    selectedYear
                )
                ageInput.text = formattedDate

                // Save the selected date for ISO conversion
                calendar.set(selectedYear, selectedMonth, selectedDay)
                ageInput.tag = calendar.time // Use the tag to store the Date object
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}

