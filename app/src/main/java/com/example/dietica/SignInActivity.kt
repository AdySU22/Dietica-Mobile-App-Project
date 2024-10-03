package com.example.dietica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val btnLogin: Button = findViewById(R.id.btnLogin)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val signUpAccountText: TextView = findViewById(R.id.signUpAccountText)

        btnLogin.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

//        forgotPasswordText.setOnClickListener {
//            forgotPassword()
//        }

        signUpAccountText.setOnClickListener {
            signUpAccount()
        }
    }

    fun signUpAccount() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}