package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.ForgotPasswordServices

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var btnGetCode: Button
    private val forgotPasswordServices = ForgotPasswordServices()  // Create an instance of the service class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        emailEditText = findViewById(R.id.emailInput)
        btnGetCode = findViewById(R.id.btnGetCode)

        btnGetCode.setOnClickListener {
            val email = emailEditText.text.toString()

            // Validate the email input
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use the ForgotPasswordServices class to handle OTP request
            forgotPasswordServices.sendOtpRequest(this, email)
        }
    }
}
