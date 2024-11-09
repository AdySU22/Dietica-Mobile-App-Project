package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Delay for splash screen for 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
            val authId = sharedPreferences.getString("authId", null)
            Log.d("OpeningPageActivity", authId ?: "Token is null")

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
    }
}