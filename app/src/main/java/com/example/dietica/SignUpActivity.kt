package com.example.dietica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val btnRegister: Button = findViewById(R.id.btnRegister)
        val loginAccountText: TextView = findViewById(R.id.loginAccountText)


        btnRegister.setOnClickListener {
            val intent = Intent(this, OTPVerification::class.java)
            startActivity(intent)
        }

        loginAccountText.setOnClickListener {
            loginAccount()
        }
    }

    fun loginAccount() {
val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

}