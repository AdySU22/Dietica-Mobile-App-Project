package com.example.dietica.services

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.dietica.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class SignInServices(
    private val activity: Activity,
    private val auth: FirebaseAuth
) {

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, onResult: (Boolean, FirebaseUser?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                firebaseAuthWithGoogle(it, onResult)
            } ?: run {
                Log.w("SignInServices", "Google Sign-In failed: account is null")
                onResult(false, null)
            }
        } catch (e: ApiException) {
            Log.e("SignInServices", "Google Sign-In failed", e)
            onResult(false, null)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, FirebaseUser?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("SignInServices", "Firebase authentication successful: ${user?.uid}")
                    onResult(true, user)
                } else {
                    Log.e("SignInServices", "Firebase authentication failed", task.exception)
                    onResult(false, null)
                }
            }
    }
}
