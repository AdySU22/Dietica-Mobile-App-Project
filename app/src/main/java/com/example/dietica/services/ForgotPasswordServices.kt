package com.example.dietica.services

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.dietica.ResetPasswordActivity
import com.google.firebase.functions.FirebaseFunctions

class ForgotPasswordServices {

    private val mFunctions = FirebaseFunctions.getInstance()

    // Function to handle sending OTP request
    fun sendOtpRequest(context: Context, email: String, onResult: () -> Unit) {
        val data = hashMapOf("email" to email)

        mFunctions
            .getHttpsCallable("forgotPassword")  // Corresponds to your Firebase function
            .call(data)
            .addOnSuccessListener {
                // On success, show a success message and navigate to ResetPasswordActivity
                Toast.makeText(context, "OTP sent to your email!", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, ResetPasswordActivity::class.java).apply {
                    putExtra("email", email)  // Pass email to ResetPasswordActivity
                }
                context.startActivity(intent)
            }
            .addOnFailureListener { e ->
                // On failure, show an error message
                Toast.makeText(context, "Failed to send OTP: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                onResult()
            }
    }
}
