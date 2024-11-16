package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.SignInServices
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var signInServices: SignInServices

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        // Initialize FirebaseAuth and SecureTokenManager
        val auth = FirebaseAuth.getInstance()
        signInServices = SignInServices(this, auth)
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
                        // Store authId to SharedPreferences
                        val sharedPreferences =
                            getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("authId", uid)
                        editor.apply()

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
        if (uid != null) {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("authId", uid)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show()
        }
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
                    val sharedPreferences =
                        getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("authId", uid)
                    editor.apply()
                    proceedToNextActivity(uid)
                } else {
                    Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
