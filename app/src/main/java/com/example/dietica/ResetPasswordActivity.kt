package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.text.*
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.OTPResetPasswordServices
import com.google.firebase.functions.FirebaseFunctionsException

class ResetPasswordActivity : BaseActivity() {

    private lateinit var otpInput1: EditText
    private lateinit var otpInput2: EditText
    private lateinit var otpInput3: EditText
    private lateinit var otpInput4: EditText
    private lateinit var btnVerify: Button
    private lateinit var email: String

    private val otpService = OTPResetPasswordServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        otpInput1 = findViewById(R.id.et_otp_1)
        otpInput2 = findViewById(R.id.et_otp_2)
        otpInput3 = findViewById(R.id.et_otp_3)
        otpInput4 = findViewById(R.id.et_otp_4)
        btnVerify = findViewById(R.id.btnResetPassword)

        email = intent.getStringExtra("email") ?: ""

        setupOTPInputs()

        btnVerify.setOnClickListener { verifyOTP() }
    }

    private fun setupOTPInputs() {
        // Helper function to move focus to the next EditText
        fun moveToNextField(current: EditText, next: EditText?) {
            current.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        next?.requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        moveToNextField(otpInput1, otpInput2)
        moveToNextField(otpInput2, otpInput3)
        moveToNextField(otpInput3, otpInput4)

        otpInput4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    otpInput4.clearFocus()
                    // Hide the keyboard
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(otpInput4.windowToken, 0)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun verifyOTP() {
        val otp = otpInput1.text.toString().trim() +
                otpInput2.text.toString().trim() +
                otpInput3.text.toString().trim() +
                otpInput4.text.toString().trim()

        if (otp.length < 4) {
            Toast.makeText(this, "Please enter all OTP digits", Toast.LENGTH_SHORT).show()
            return
        }

        otpService.verifyOtp(email, otp)
            .addOnSuccessListener { result ->
                val isValid = result["valid"] as? Boolean ?: false
                Log.e("OTP-Validation", isValid.toString())

                if (isValid) {
                    Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, UpdatePasswordActivity::class.java).apply {
                        putExtra("email", email)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ResetPasswordActivity", "Error: ", e)
                val errorMessage = when (e) {
                    is FirebaseFunctionsException -> when (e.code) {
                        FirebaseFunctionsException.Code.NOT_FOUND -> "No OTP found for this email"
                        FirebaseFunctionsException.Code.FAILED_PRECONDITION -> "OTP has expired"
                        FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> "Maximum OTP attempts exceeded"
                        else -> e.message ?: "Error verifying OTP"
                    }
                    else -> "Unknown error occurred"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
    }
}
