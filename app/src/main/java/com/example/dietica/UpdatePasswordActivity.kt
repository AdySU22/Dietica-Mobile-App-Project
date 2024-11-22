package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.UpdatePasswordServices

class UpdatePasswordActivity : BaseActivity() {

    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var btnUpdatePassword: Button
    private val updatePasswordServices = UpdatePasswordServices()  // Create instance of the service class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_password)

        passwordEditText = findViewById(R.id.newPasswordInput)
        confirmPasswordEditText = findViewById(R.id.newRePasswordInput)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)

        btnUpdatePassword.setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

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

            // Use the service class to handle password update
            updatePasswordServices.updatePassword(this, email, password)
        }
    }
}
