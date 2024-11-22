package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.GeneralInformationServices
import com.example.dietica.services.LoadingUtils
import com.google.firebase.auth.FirebaseAuth

class GeneralInformationActivity : BaseActivity() {

    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var btnNext: Button
    private lateinit var email: String

    private lateinit var progressOverlay: View

    private val generalInfoService = GeneralInformationServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_information)

        // Initialize loading
        progressOverlay = findViewById(R.id.progress_overlay)

        email = intent.getStringExtra("email") ?: ""
        if (email.isEmpty()) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        btnNext = findViewById(R.id.btnNext)

        btnNext.setOnClickListener { validateAndProceed() }
    }

    private fun validateAndProceed() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        btnNext.isEnabled = false

        // Start loading overlay
        LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)

        // Register the user with Firebase Auth
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { authTask ->
                if (authTask.isSuccessful) {
                    // Registration successful, now we proceed
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser

                    user?.let {
                        // User is authenticated, let's save the authId (UID)
                        val authId = it.uid

                        // Save authId to SharedPreferences for future use
                        val sharedPref = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("authId", authId)
                        editor.apply()

                        // Pass authId to TellMeActivity
                        val intent = Intent(this, TellMeActivity::class.java)
                        intent.putExtra("authId", authId)
                        intent.putExtra("email", email)
                        intent.putExtra("firstName", firstName)
                        intent.putExtra("lastName", lastName)
                        startActivity(intent)
                        finish()
                    }

                    // Optional: You can use your GeneralInfoService here if needed
                    Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show()

                } else {
                    // Registration failed, handle the error
                    Toast.makeText(this, "Registration Failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                }

                // Hide loading overlay
                LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)

                btnNext.isEnabled = true
            }
    }
}
