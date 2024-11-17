package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.SignInServices
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn

class SignInActivity : AppCompatActivity() {

    private lateinit var signInServices: SignInServices

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            signInServices.handleGoogleSignInResult(task) { success, uid ->
                Log.d("SignInActivity", "Google Sign-in success: $success, uid: $uid")
                if (success && uid != null) {
                    proceedToHomeActivity(uid)
                } else {
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

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
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                // Save authId to SharedPreferences
                                val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                                sharedPreferences.edit().putString("authId", currentUser.uid).apply()

                                // Proceed to HomeActivity
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()  // Close SignInActivity so user cannot go back to sign in
                            }
                        } else {
                            Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
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

    private fun proceedToHomeActivity(authId: String) {
        Log.d("SignInActivity", "Proceeding to HomeActivity with authId: $authId")
        if (authId.isNotEmpty()) {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("authId", authId)
            }
            startActivity(intent)
            finish()
        } else {
            Log.e("SignInActivity", "Invalid authId, unable to proceed.")
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
        googleSignInLauncher.launch(signInIntent)
    }
}
