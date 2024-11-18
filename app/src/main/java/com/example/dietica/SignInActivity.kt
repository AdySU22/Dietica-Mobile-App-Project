package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dietica.services.SignInServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : BaseActivity() {

    private lateinit var signInServices: SignInServices
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "SignInActivity"
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Google Sign-In result received: resultCode=${result.resultCode}, data=${result.data}")
        if (result.resultCode == RESULT_OK && result.data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            Log.d(TAG, "Google Sign-In task received. Processing...")
            handleGoogleSignInResult(task) { success, uid ->
                if (success && uid != null) {
                    Log.d(TAG, "Google Sign-In successful. UID: $uid")
                    proceedToHomeActivity(uid)
                } else {
                    Log.e(TAG, "Google Sign-In failed.")
                    Toast.makeText(this, "Google Sign-In failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e(TAG, "Google Sign-In canceled or failed.")
            Toast.makeText(this, "Google Sign-In canceled. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        Log.d(TAG, "SignInActivity created. Initializing services...")

        auth = FirebaseAuth.getInstance()
        signInServices = SignInServices(this, auth)
        signInServices.initializeGoogleSignInClient(getString(R.string.default_web_client_id))
        googleSignInClient = signInServices.googleSignInClient
        Log.d(TAG, "Google Sign-In client initialized with Web Client ID: ${getString(R.string.default_web_client_id)}")

        val btnLogin: Button = findViewById(R.id.btnLogin)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val signUpAccountText: TextView = findViewById(R.id.signUpAccountText)
        val googleSignInButton: ImageView = findViewById(R.id.googleSignInButton)
        val emailField: EditText = findViewById(R.id.emailInput)
        val passwordField: EditText = findViewById(R.id.passwordInput)

        btnLogin.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            Log.d(TAG, "Attempting email/password sign-in. Email: $email")
            if (isValidInput(email, password)) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                Log.d(TAG, "Email/password sign-in successful. UID: ${currentUser.uid}")
                                val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                                sharedPreferences.edit().putString("authId", currentUser.uid).apply()
                                proceedToHomeActivity(currentUser.uid)
                            }
                        } else {
                            Log.e(TAG, "Email/password sign-in failed.", task.exception)
                            Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Log.w(TAG, "Invalid email or password input.")
                Toast.makeText(this, "Please enter valid email and password", Toast.LENGTH_SHORT).show()
            }
        }

        forgotPasswordText.setOnClickListener {
            Log.d(TAG, "Navigating to ForgotPasswordActivity")
            forgotPassword()
        }

        signUpAccountText.setOnClickListener {
            Log.d(TAG, "Navigating to SignUpActivity")
            signUpAccount()
        }

        googleSignInButton.setOnClickListener {
            Log.d(TAG, "Launching Google Sign-In.")
            signInWithGoogle()
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, onResult: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful. ID Token: ${account.idToken}")
                firebaseAuthWithGoogle(account, onResult)
            } else {
                Log.e(TAG, "Google Sign-In account is null")
                onResult(false, null)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed with exception: ${e.statusCode}", e)
            onResult(false, null)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val idToken = account.idToken
        Log.d(TAG, "Attempting Firebase auth with Google. ID Token: $idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d(TAG, "Firebase authentication successful. UID: ${user?.uid}")
                onResult(true, user?.uid)
            } else {
                Log.e(TAG, "Firebase authentication failed", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Log.e(TAG, "ID Token is invalid or expired.")
                    googleSignInClient.signOut().addOnCompleteListener {
                        onResult(false, null)
                    }
                } else {
                    onResult(false, null)
                }
            }
        }
    }



    private fun isValidInput(email: String, password: String): Boolean {
        val isValid = email.isNotEmpty() && password.isNotEmpty()
        Log.d(TAG, "Input validation result: $isValid")
        return isValid
    }

    private fun proceedToHomeActivity(authId: String) {
        Log.d(TAG, "Proceeding to HomeActivity with authId: $authId")
        if (authId.isNotEmpty()) {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("authId", authId)
            }
            startActivity(intent)
            finish()
        } else {
            Log.e(TAG, "Invalid authId. Unable to proceed to HomeActivity.")
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
