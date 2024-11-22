package com.example.dietica

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat

class PrivacyPolicyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacy_policy)

        val backBtn: Button = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, TermsAndConditionsActivity::class.java)
            startActivity(intent)
        }

        val agreeBtn: Button = findViewById(R.id.agreeBtn)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

        // Set agreeBtn initially disabled
        agreeBtn.isEnabled = false
        agreeBtn.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_gray))

        // Enable the button when the user scrolls to the bottom
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY >= scrollView.getChildAt(0).measuredHeight - scrollView.measuredHeight) {
                // Enable the button
                agreeBtn.isEnabled = true
                agreeBtn.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_light_blue))
            }
        }

        // Action when agreeBtn is clicked
        agreeBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
