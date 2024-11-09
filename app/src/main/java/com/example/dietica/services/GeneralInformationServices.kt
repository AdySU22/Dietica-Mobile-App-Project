package com.example.dietica.services

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException

class GeneralInformationServices {

    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

    fun finalizeSignup(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val data = hashMapOf(
            "email" to email,
            "password" to password,
            "confirmPassword" to confirmPassword,
            "firstName" to firstName,
            "lastName" to lastName
        )

        functions
            .getHttpsCallable("finalizeSignup")
            .call(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    val e = task.exception
                    val errorMessage = if (e is FirebaseFunctionsException) {
                        when (e.code) {
                            FirebaseFunctionsException.Code.ALREADY_EXISTS -> "User already exists"
                            FirebaseFunctionsException.Code.PERMISSION_DENIED -> "Your request is unauthorized"
                            FirebaseFunctionsException.Code.INVALID_ARGUMENT -> "Invalid input data"
                            else -> e.message ?: "Error completing registration"
                        }
                    } else {
                        "An unexpected error occurred"
                    }
                    onComplete(false, errorMessage)
                }
            }
    }
}
