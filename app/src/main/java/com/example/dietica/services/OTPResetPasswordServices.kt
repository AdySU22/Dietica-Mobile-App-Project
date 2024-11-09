package com.example.dietica.services

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.android.gms.tasks.Task

class OTPResetPasswordServices {

    private val functions = FirebaseFunctions.getInstance()

    fun verifyOtp(email: String, otp: String): Task<Map<*, *>> {
        val data = hashMapOf(
            "email" to email,
            "otp" to otp
        )

        return functions
            .getHttpsCallable("verifyOtp")
            .call(data)
            .continueWith { task ->
                if (task.isSuccessful) {
                    task.result?.data as? Map<*, *> ?: throw Exception("Invalid response from server")
                } else {
                    throw task.exception ?: Exception("Unknown error occurred")
                }
            }
    }
}
