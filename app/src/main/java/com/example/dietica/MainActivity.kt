package com.example.dietica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Check if notification permission is needed (Android 13 and above)
        requestNotificationPermission()

        functions = FirebaseFunctions.getInstance()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserToken", "Token: ${task.result}")
                val data = mapOf(
                    "authId" to "authId-test-0", // TODO Replace with actual authId
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
        
        // Delay for splash screen for 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OpeningPageActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }

    private fun requestNotificationPermission() {
        // Check if notification permission is needed (Android 13 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_DENIED
            ) {
                // Request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }
}