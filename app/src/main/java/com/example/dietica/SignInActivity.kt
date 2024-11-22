package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.dietica.services.SignInServices
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var signInServices: SignInServices
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        forgotPasswordText.setOnClickListener {
            val forgotPasswordIntent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgotPasswordIntent)
        }
        
        val auth = FirebaseAuth.getInstance()
        signInServices = SignInServices(this, auth)

        val btnGoogleSignIn: ImageView = findViewById(R.id.googleSignInButton)
        btnGoogleSignIn.setOnClickListener {
            val signInIntent = signInServices.getGoogleSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            signInServices.handleGoogleSignInResult(task) { success, user ->
                if (success && user != null) {
                    Toast.makeText(this, "Sign in successful: ${user.email}", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity(user.uid, user.email, user.displayName)
                } else {
                    Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMainActivity(userId: String?, email: String?, displayName: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("authId", userId)
            putExtra("email", email)
            putExtra("firstName", displayName)
        }
        startActivity(intent)
        finish()
    }
}
