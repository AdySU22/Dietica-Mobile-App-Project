package com.example.dietica.services

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.dietica.SignInActivity
import com.google.firebase.functions.FirebaseFunctions

class UpdatePasswordServices {

    private val mFunctions = FirebaseFunctions.getInstance()

    // Function to handle password update
    fun updatePassword(context: Context, email: String, password: String) {
        val data = hashMapOf("email" to email, "password" to password, "confirmPassword" to password)

        mFunctions
            .getHttpsCallable("resetPassword")  // Corresponds to your Firebase function
            .call(data)
            .addOnSuccessListener {
                // On success, show a message and redirect to SignInActivity
                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, SignInActivity::class.java)
                context.startActivity(intent)
            }
            .addOnFailureListener { e ->
                // On failure, show an error message
                Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
