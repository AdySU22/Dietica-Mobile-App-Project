package com.example.dietica.services

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.functions.FirebaseFunctions
import org.json.JSONObject

class SignInServices(
    private val activity: Activity,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {

    private lateinit var googleSignInClient: GoogleSignInClient

    fun initializeGoogleSignInClient(webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    fun signInWithEmailAndPassword(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val data = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        functions.getHttpsCallable("signin").call(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result?.data as HashMap<*, *>
                val message = result["message"].toString()
                val uid = result["uid"].toString()
                val idToken = result["idToken"].toString()
                onResult(true, uid)
            } else {
                Log.e("SignInServices", "Error signing in", task.exception)
                onResult(false, null)
            }
        }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, onResult: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account, onResult)
        } catch (e: ApiException) {
            Log.w("SignInServices", "Google sign-in failed", e)
            onResult(false, null)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                onResult(true, user?.uid)
            } else {
                Log.w("SignInServices", "Firebase authentication with Google failed", task.exception)
                onResult(false, null)
            }
        }
    }
}
