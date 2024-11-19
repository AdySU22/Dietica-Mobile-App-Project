package com.example.dietica.services

import android.app.Activity
import android.util.Log
import com.example.dietica.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInServices(
    private val activity: Activity,
    private val auth: FirebaseAuth
) {

    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "SignInServices"

        /**
         * Provides GoogleSignInOptions for the application.
         */
        fun getGoogleSignInOptions(activity: Activity): GoogleSignInOptions {
            val webClientId = activity.getString(R.string.default_web_client_id)
            return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        }
    }

    /**
     * Initializes the GoogleSignInClient with the provided web client ID.
     */
    fun initializeGoogleSignInClient(webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        Log.d(TAG, "GoogleSignInClient initialized with Web Client ID: $webClientId")
    }

    /**
     * Returns the Google Sign-In intent to launch the sign-in flow.
     */
    fun getGoogleSignInIntent(): android.content.Intent = googleSignInClient.signInIntent

    /**
     * Handles the result from the Google Sign-In intent and authenticates with Firebase.
     */
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, onResult: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful. Authenticating with Firebase...")
                firebaseAuthWithGoogle(account, onResult)
            } else {
                Log.w(TAG, "Google Sign-In account is null.")
                onResult(false, null)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed with exception: ${e.statusCode}", e)
            onResult(false, null)
        }
    }

    /**
     * Authenticates with Firebase using the Google Sign-In account.
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val idToken = account.idToken
        Log.d(TAG, "Authenticating with Firebase using ID Token: $idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d(TAG, "Firebase authentication successful. User ID: ${user?.uid}")
                onResult(true, user?.uid)
            } else {
                Log.e(TAG, "Firebase authentication failed.", task.exception)
                onResult(false, null)
            }
        }
    }
}
