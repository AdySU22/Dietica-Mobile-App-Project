package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.dietica.services.OTPSignUpServices

class OTPVerification : BaseActivity() {

    private lateinit var otpInput1: EditText
    private lateinit var otpInput2: EditText
    private lateinit var otpInput3: EditText
    private lateinit var otpInput4: EditText
    private lateinit var btnVerify: Button
    private lateinit var email: String

    private val otpService = OTPSignUpServices()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otp_verification)

        otpInput1 = findViewById(R.id.et_otp_1)
        otpInput2 = findViewById(R.id.et_otp_2)
        otpInput3 = findViewById(R.id.et_otp_3)
        otpInput4 = findViewById(R.id.et_otp_4)
        btnVerify = findViewById(R.id.btnConfirm)

        email = intent.getStringExtra("email") ?: ""

        setupOTPInputs()

        btnVerify.setOnClickListener { verifyOTP() }
    }

    private fun setupOTPInputs() {
        // Helper function to handle focus moving forward and backward
        fun setupFieldNavigation(current: EditText, previous: EditText?, next: EditText?) {
            current.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to next field if character is added
                        next?.requestFocus()
                    } else if (s?.isEmpty() == true && before > 0) {
                        // Move to previous field if character is deleted
                        previous?.requestFocus()
                    }

                    // Check if all fields are filled
                    checkAllFieldsAndVerify()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            current.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (current.text.isEmpty()) {
                        previous?.requestFocus()
                    }
                }
                false
            }
        }

        setupFieldNavigation(otpInput1, null, otpInput2)
        setupFieldNavigation(otpInput2, otpInput1, otpInput3)
        setupFieldNavigation(otpInput3, otpInput2, otpInput4)
        setupFieldNavigation(otpInput4, otpInput3, null)
    }

    private fun checkAllFieldsAndVerify() {
        val otp1 = otpInput1.text.toString().trim()
        val otp2 = otpInput2.text.toString().trim()
        val otp3 = otpInput3.text.toString().trim()
        val otp4 = otpInput4.text.toString().trim()

        if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(otpInput4.windowToken, 0)

            btnVerify.performClick()
        }
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

        otpService.verifyOtp(email, otp) { isValid, errorMessage ->
            if (isValid) {
                Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, GeneralInformationActivity::class.java).apply {
                    putExtra("email", email)
                }
                startActivity(intent)
                finish()
            } else {
                errorMessage?.let {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()

                clearOtpInput()
            }
        }
    }

    private fun clearOtpInput() {
        otpInput1.text.clear()
        otpInput2.text.clear()
        otpInput3.text.clear()
        otpInput4.text.clear()
    }
}
