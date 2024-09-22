package com.example.dietica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class OpeningPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening_page)

        val getStartedButton: Button = findViewById(R.id.getStartedButton)
        getStartedButton.backgroundTintList = null

        // Set an onClickListener to the button to navigate to the next activity
        getStartedButton.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}