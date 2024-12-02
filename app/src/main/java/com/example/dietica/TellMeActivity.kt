package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.dietica.services.LoadingUtils
import com.google.firebase.functions.FirebaseFunctions

class TellMeActivity : BaseActivity() {

    private lateinit var ageInput: EditText
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
        ageInput = findViewById(R.id.ageInput)
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
            val email = intent.getStringExtra("email")
            val firstName = intent.getStringExtra("firstName")
            val lastName = intent.getStringExtra("lastName")
            val data = hashMapOf(
                "authId" to authId,
                "email" to email,
                "firstName" to firstName,
                "lastName" to lastName,
                "weight" to weightInput.text.toString().toDoubleOrNull(),
                "height" to heightInput.text.toString().toDoubleOrNull(),
                "gender" to genderSpinner.selectedItem.toString(),
                "activityLevels" to activityLevelSpinner.selectedItem.toString(),
                "illnesses" to chronicIllnessInput.text.toString().takeIf { it.isNotEmpty() }
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

