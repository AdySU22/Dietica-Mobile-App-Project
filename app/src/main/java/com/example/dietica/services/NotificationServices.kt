package com.example.dietica.services

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging

class NotificationServices(private val activity: Activity) {

    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the notification permission is already granted
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_DENIED
            ) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    fun setUserToken(authId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserToken", "Token: ${task.result}")
                val data = mapOf(
                    "authId" to authId, // Replace with actual authId if needed
                    "token" to task.result
                )
                functions.getHttpsCallable("setUserToken")
                    .call(data)
                    .addOnCompleteListener {
                        Log.d("UserToken", "Token set successfully")
                    }
                    .addOnCanceledListener {
                        Log.w("UserToken", "Token sent failed")
                    }
            } else {
                Log.w("UserToken", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }
}