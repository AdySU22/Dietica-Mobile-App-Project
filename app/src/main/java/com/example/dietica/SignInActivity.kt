package com.example.dietica

import SecureTokenManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.SignInServices
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var signInServices: SignInServices
    private lateinit var secureTokenManager: SecureTokenManager  // Declare SecureTokenManager

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize FirebaseAuth and SecureTokenManager
        val auth = FirebaseAuth.getInstance()
        signInServices = SignInServices(this, auth)
        secureTokenManager = SecureTokenManager(this) // Initialize SecureTokenManager
        signInServices.initializeGoogleSignInClient(getString(R.string.default_web_client_id))

        val btnLogin: Button = findViewById(R.id.btnLogin)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val signUpAccountText: TextView = findViewById(R.id.signUpAccountText)
        val googleSignInButton: ImageView = findViewById(R.id.googleSignInButton)
        val emailField: EditText = findViewById(R.id.emailInput)
        val passwordField: EditText = findViewById(R.id.passwordInput)

        btnLogin.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            if (isValidInput(email, password)) {
                signInServices.signInWithEmailAndPassword(email, password) { success, uid ->
                    if (success) {
                        // Save the user's token securely
                        secureTokenManager.saveToken(uid ?: "")
                        proceedToNextActivity(uid)
                    } else {
                        Toast.makeText(this, "Sign in failed: Incorrect Email or Password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        forgotPasswordText.setOnClickListener { forgotPassword() }
        signUpAccountText.setOnClickListener { signUpAccount() }
        googleSignInButton.setOnClickListener { signInWithGoogle() }
    }

    private fun isValidInput(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    private fun proceedToNextActivity(uid: String?) {
        val intent = Intent(this, TellMeActivity::class.java).apply {
            putExtra("uid", uid)
        }
        startActivity(intent)
        finish()
    }

    private fun signUpAccount() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private fun forgotPassword() {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }

    private fun signInWithGoogle() {
        val signInIntent = signInServices.getGoogleSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            signInServices.handleGoogleSignInResult(task) { success, uid ->
                if (success) {
                    // Save the Google sign-in token securely
                    secureTokenManager.saveToken(uid ?: "")
                    proceedToNextActivity(uid)
                } else {
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
