package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.functions.FirebaseFunctions

class TellMeActivity : AppCompatActivity() {

    private lateinit var ageInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var chronicIllnessInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var btnLetsGetStarted: Button
    private lateinit var firebaseFunctions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tell_me)

        // Initialize Views
        ageInput = findViewById(R.id.ageInput)
        heightInput = findViewById(R.id.heightInput)
        weightInput = findViewById(R.id.weightInput)
        chronicIllnessInput = findViewById(R.id.chronicIllnessInput)
        genderSpinner = findViewById(R.id.genderInput)
        activityLevelSpinner = findViewById(R.id.activityLevelInput)
        btnLetsGetStarted = findViewById(R.id.btnLetsGetStarted)

        // Initialize Firebase Functions
        firebaseFunctions = FirebaseFunctions.getInstance()

        // Set up Adapters for Spinners
        setupSpinnerAdapter(genderSpinner, R.array.gender_options)
        setupSpinnerAdapter(activityLevelSpinner, R.array.activity_level_options)

        // Get the authId from Intent or SharedPreferences
        val authId = intent.getStringExtra("authId")
        if (authId != null) {
            // Log the received authId
            Log.d("TellMeActivity", "Received authId: $authId")
        } else {
            // Handle the case where authId is missing
            Toast.makeText(this, "AuthId is missing!", Toast.LENGTH_SHORT).show()
        }

        // Check if authId is valid
        if (authId != null) {
            // Proceed with form filling logic
            setupListeners(authId)
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Validate inputs initially to enable button
        validateInputs()

        // Add listeners to inputs for real-time validation
        ageInput.addTextChangedListener { validateInputs() }
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
    }

    private fun setupListeners(authId: String) {
        // Add listeners and validation logic for button click
        btnLetsGetStarted.setOnClickListener {
            val data = hashMapOf(
                "authId" to authId,
                "weight" to weightInput.text.toString().toDoubleOrNull(),
                "height" to heightInput.text.toString().toDoubleOrNull(),
                "gender" to genderSpinner.selectedItem.toString(),
                "activityLevels" to activityLevelSpinner.selectedItem.toString(),
                "illnesses" to chronicIllnessInput.text.toString().takeIf { it.isNotEmpty() }
            )

            // Call the Firebase function
            firebaseFunctions
                .getHttpsCallable("setUserPhysical")
                .call(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result?.data as? Map<*, *>
                        val message = result?.get("message") as? String
                        val id = result?.get("id") as? String
                        Toast.makeText(this, "$message (ID: $id)", Toast.LENGTH_LONG).show()
                        // Navigate to HomeActivity
                        startActivity(Intent(this, HomeActivity::class.java))
                    } else {
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                    }
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
        val ageNotEmpty = ageInput.text.isNotEmpty()
        val heightNotEmpty = heightInput.text.toString().toDoubleOrNull() != null
        val weightNotEmpty = weightInput.text.toString().toDoubleOrNull() != null
        val chronicIllnessNotEmpty = chronicIllnessInput.text.isNotEmpty()

        val genderSelected = genderSpinner.selectedItemPosition != 0  // 0 means "Select Gender"
        val activitySelected = activityLevelSpinner.selectedItemPosition != 0

        val isButtonEnabled = ageNotEmpty && heightNotEmpty && weightNotEmpty &&
                chronicIllnessNotEmpty && genderSelected && activitySelected

        btnLetsGetStarted.isEnabled = isButtonEnabled
    }
}

