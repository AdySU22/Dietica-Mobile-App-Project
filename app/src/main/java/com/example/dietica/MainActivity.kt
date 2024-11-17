package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

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