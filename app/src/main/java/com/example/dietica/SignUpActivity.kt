package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dietica.services.SignUpServices
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task

class SignUpActivity : BaseActivity() {

    private lateinit var signUpServices: SignUpServices
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        signUpServices = SignUpServices(this)

        val btnRegister: Button = findViewById(R.id.btnRegister)
        val loginAccountText: TextView = findViewById(R.id.loginAccountText)
        val emailEditText: EditText = findViewById(R.id.emailInput)
        val googleSignInButton: ImageView = findViewById(R.id.googleSignInButton)

        googleSignInButton.setOnClickListener {
            startActivityForResult(signUpServices.getGoogleSignInIntent(), RC_SIGN_IN)
        }

        btnRegister.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {
                signUpServices.requestOtp(email) { message ->
                    message?.let {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, OTPVerification::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(this, "Failed to request OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            }
        }

        loginAccountText.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            signUpServices.handleSignInResult(task) { user ->
                if (user != null) {
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TellMeActivity::class.java)
                    intent.putExtra("authId", user.uid)
                    intent.putExtra("email", user.email)
                    intent.putExtra("firstName", user.displayName)
                    intent.putExtra("lastName", "")
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
