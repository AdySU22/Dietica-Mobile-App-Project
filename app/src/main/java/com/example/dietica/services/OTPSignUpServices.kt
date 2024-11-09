package com.example.dietica.services

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException

class OTPSignUpServices {

    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

    fun verifyOtp(email: String, otp: String, onComplete: (Boolean, String?) -> Unit) {
        val data = hashMapOf(
            "email" to email,
            "otp" to otp
        )

        functions
            .getHttpsCallable("verifyOtp")
            .call(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.data as? Map<*, *>
                    val isValid = result?.get("valid") as? Boolean ?: false
                    onComplete(isValid, null)
                } else {
                    val e = task.exception
                    val errorMessage = if (e is FirebaseFunctionsException) {
                        when (e.code) {
                            FirebaseFunctionsException.Code.NOT_FOUND -> "No OTP found for this email"
                            FirebaseFunctionsException.Code.FAILED_PRECONDITION -> "OTP has expired"
                            FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> "Maximum OTP attempts exceeded"
                            else -> e.message ?: "Error verifying OTP"
                        }
                    } else {
                        "Unknown error occurred"
                    }
                    onComplete(false, errorMessage)
                }
            }
    }
}
