package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class TellMeActivity : AppCompatActivity() {

    private lateinit var ageInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var chronicIllnessInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var btnLetsGetStarted: Button

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

        // Set up Adapters for Spinners
        setupSpinnerAdapter(genderSpinner, R.array.gender_options)
        setupSpinnerAdapter(activityLevelSpinner, R.array.activity_level_options)

        // Add TextWatchers to EditText fields
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        // Attach TextWatcher to relevant fields
        ageInput.addTextChangedListener(textWatcher)
        heightInput.addTextChangedListener(textWatcher)
        weightInput.addTextChangedListener(textWatcher)
        chronicIllnessInput.addTextChangedListener(textWatcher)

        // Listen for selection changes in the Spinners to validate inputs
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                validateInputs()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        activityLevelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                validateInputs()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Handle button click to proceed to HomeActivity
        btnLetsGetStarted.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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
        val heightNotEmpty = heightInput.text.isNotEmpty()
        val weightNotEmpty = weightInput.text.isNotEmpty()
        val chronicIllnessNotEmpty = chronicIllnessInput.text.isNotEmpty()

        // Ensure that a valid gender and activity level are selected
        val genderSelected = genderSpinner.selectedItemPosition != 0
        val activitySelected = activityLevelSpinner.selectedItemPosition != 0

        // Enable the button if all inputs are valid
        /*btnLetsGetStarted.isEnabled =
            ageNotEmpty && heightNotEmpty && weightNotEmpty &&
                    chronicIllnessNotEmpty && genderSelected && activitySelected*/
    }
}
