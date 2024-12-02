package com.example.dietica

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.LoadingUtils
import com.example.dietica.services.UpdatePasswordServices

class UpdatePasswordActivity : BaseActivity() {

    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var btnUpdatePassword: Button
    private val updatePasswordServices = UpdatePasswordServices()  // Create instance of the service class

    private lateinit var progressOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_password)

        passwordEditText = findViewById(R.id.newPasswordInput)
        confirmPasswordEditText = findViewById(R.id.newRePasswordInput)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)

        // Initialize loading
        progressOverlay = findViewById(R.id.progress_overlay)

        btnUpdatePassword.setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (!isPasswordValid(password)) {
                Toast.makeText(
                    this,
                    "Password must include at least one uppercase letter, one lowercase letter, one number, and one special character",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the email from the intent
            val email = intent.getStringExtra("email")
            if (email == null) {
                Toast.makeText(this, "Email is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Start loading overlay
            LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)

            // Use the service class to handle password update
            updatePasswordServices.updatePassword(this, email, password, {
                // Hide loading overlay
                LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)
            })
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return regex.matches(password)
    }
}
