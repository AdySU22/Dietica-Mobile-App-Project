package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.text.*
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import com.example.dietica.services.LoadingUtils
import com.example.dietica.services.OTPSignUpServices

class OTPVerification : BaseActivity() {

    private lateinit var otpInput1: EditText
    private lateinit var otpInput2: EditText
    private lateinit var otpInput3: EditText
    private lateinit var otpInput4: EditText
    private lateinit var btnVerify: Button
    private lateinit var email: String

    private lateinit var progressOverlay: View

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

        // Initialize loading overlay
        progressOverlay = findViewById(R.id.progress_overlay)

        setupOTPInputs()

        btnVerify.setOnClickListener { verifyOTP() }
    }

    private fun setupOTPInputs() {
        // Helper function to move focus to the next or previous EditText
        fun moveToNextField(current: EditText, next: EditText?, previous: EditText?) {
            current.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        next?.requestFocus()
                        checkAllFieldsFilledAndClickConfirm()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            current.setOnKeyListener {_, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && current.text.isEmpty()) {
                    previous?.requestFocus()
                    true
                } else {
                    false
                }
            }
        }

        // Set up focus movement
        moveToNextField(otpInput1, otpInput2, null)
        moveToNextField(otpInput2, otpInput3, otpInput1)
        moveToNextField(otpInput3, otpInput4, otpInput2)
        moveToNextField(otpInput4, null, otpInput3)

        otpInput4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    otpInput4.clearFocus()
                    // Hide the keyboard
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(otpInput4.windowToken, 0)
                    checkAllFieldsFilledAndClickConfirm()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkAllFieldsFilledAndClickConfirm() {
        val otp1 = otpInput1.text.toString().trim()
        val otp2 = otpInput2.text.toString().trim()
        val otp3 = otpInput3.text.toString().trim()
        val otp4 = otpInput4.text.toString().trim()

        if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty()) {
            btnVerify.callOnClick() // Programmatically simulates the Confirm button click
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

        // Start loading overlay
        LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
        otpService.verifyOtp(email, otp) { isValid, errorMessage ->
            if (isValid) {
                Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, GeneralInformationActivity::class.java).apply {
                    putExtra("email", email)
                }
                startActivity(intent)
                finish()
            } else {
                // Clear OTP inputs on failure
                clearOtpFields()
                errorMessage?.let {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
            // Hide loading overlay
            LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)
        }
    }

    private fun clearOtpFields() {
        otpInput1.text.clear()
        otpInput2.text.clear()
        otpInput3.text.clear()
        otpInput4.text.clear()
        otpInput1.requestFocus()
    }
}
