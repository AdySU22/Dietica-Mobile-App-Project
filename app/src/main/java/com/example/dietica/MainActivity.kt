package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.dietica.services.NotificationServices

class MainActivity : AppCompatActivity() {

    private lateinit var notificationServices: NotificationServices

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
        val authId = sharedPreferences.getString("authId", null)
        Log.d("OpeningPageActivity", authId ?: "Token is null")

        // Delay for splash screen for 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (authId != null) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, OpeningPageActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)

        // If user is logged in
        if (authId != null) {
            notificationServices = NotificationServices(this)
            notificationServices.requestNotificationPermission()
            notificationServices.setUserToken(authId)
        }
    }
}