package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.activity.enableEdgeToEdge

class OpeningPageActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_opening_page)

        val getStartedButton: Button = findViewById(R.id.getStartedButton)
        getStartedButton.backgroundTintList = null

        // Set an onClickListener to the button to navigate to the next activity
        getStartedButton.setOnClickListener {
            val intent = Intent(this, TermsAndConditionsActivity::class.java)
            startActivity(intent)
        }
    }
}