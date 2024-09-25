package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class OTPVerification : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otp_verification)

        val btnConfirm: Button = findViewById(R.id.btnConfirm)
        btnConfirm.setOnClickListener {
            val intent = Intent(this, GeneralInformationActivity::class.java)
            startActivity(intent)
        }
    }
}