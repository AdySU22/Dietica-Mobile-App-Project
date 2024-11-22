package com.example.dietica

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.dietica.services.SignUpServices
import com.google.android.gms.auth.api.signin.GoogleSignIn


class SignUpActivity : BaseActivity() {

    private lateinit var signUpServices: SignUpServices
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        signUpServices = SignUpServices(this)

        val btnGoogleSignIn: ImageView = findViewById(R.id.googleSignInButton)
        val btnRegister: Button = findViewById(R.id.btnRegister)
        val loginAccountText: TextView = findViewById(R.id.loginAccountText)
        val emailEditText: EditText = findViewById(R.id.emailInput)
        val termsAndConditionCheckbox: CheckBox = findViewById(R.id.termsCheckBox)
        val privacyPolicyCheckbox: CheckBox = findViewById(R.id.privacyPolicyCheckBox)
        val termsAndConditionText: TextView = findViewById(R.id.termsAndConditionText)
        val privacyPolicyText: TextView = findViewById(R.id.privacyPolicyText)

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        btnRegister.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            when {
                email.isEmpty() -> {
                    Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
                !termsAndConditionCheckbox.isChecked -> {
                    Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
                }
                !privacyPolicyCheckbox.isChecked -> {
                    Toast.makeText(this, "You must agree to the Privacy Policy", Toast.LENGTH_SHORT).show()
                }
                !isConnectedToInternet() -> {
                    Toast.makeText(this, "No internet connection. Please check your connection.", Toast.LENGTH_SHORT).show()
                }
                else -> {
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
                }
            }
        }

        loginAccountText.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        termsAndConditionText.setOnClickListener {
            startActivity(Intent(this, TermsAndConditionsActivity::class.java))
        }

        privacyPolicyText.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = signUpServices.getGoogleSignInClient().signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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


