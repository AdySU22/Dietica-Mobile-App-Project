package com.example.dietica.services

import android.content.Context
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
import com.google.firebase.functions.FirebaseFunctions

class SignUpServices(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>, onComplete: (FirebaseUser?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it, onComplete) }
        } catch (e: ApiException) {
            Log.w("SignUpServices", "Google sign in failed", e)
            onComplete(null)
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return googleSignInClient
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onComplete: (FirebaseUser?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(auth.currentUser)
            } else {
                Log.w("SignUpServices", "signInWithCredential:failure", task.exception)
                onComplete(null)
            }
        }
    }

    fun requestOtp(email: String, onComplete: (String?) -> Unit) {
        val data = hashMapOf("email" to email)
        functions.getHttpsCallable("signup")
            .call(data)
            .addOnSuccessListener { result ->
                val message = (result.data as? Map<*, *>)?.get("message") as? String
                onComplete(message)
            }
            .addOnFailureListener { e ->
                onComplete(e.localizedMessage)
            }
    }
}
