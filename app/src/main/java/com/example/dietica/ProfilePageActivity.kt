package com.example.dietica

import android.content.Intent
import android.hardware.camera2.params.BlackLevelPattern
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class ProfilePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_page)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val editProfileButton: LinearLayout = findViewById(R.id.editProfileButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            // Remove the saved authId
            val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("authId")
            editor.apply()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

    }
}
