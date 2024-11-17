package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePageActivity : AppCompatActivity() {

    private lateinit var guestUserTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_page)

        // Initialize the TextView
        guestUserTextView = findViewById(R.id.guestUserTextView)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val editProfileButton: LinearLayout = findViewById(R.id.editProfileButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        val authId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ProfilePage", "Auth ID: $authId")

        // Check if user is logged in
        if (authId != null) {
            // User is logged in, fetch their details from Firestore
            fetchUserDetails(authId)
        } else {
            // If no user is logged in, show "Guest User"
            guestUserTextView.text = "Guest User"
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            val authId = FirebaseAuth.getInstance().currentUser?.uid
            if (authId != null) {
                intent.putExtra("authId", authId)
            }
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            // Remove the saved authId
            val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("authId")
            editor.apply()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // User is logged out, handle accordingly
                Toast.makeText(this, "User is logged out", Toast.LENGTH_LONG).show()
                finish() // or redirect to login page
            }
        }
    }

    // Function to fetch user details from Firestore
    private fun fetchUserDetails(authId: String) {
        Log.d("ProfilePage", "Fetching details for user ID: $authId")  // Log the authId to verify it's correct
        val db = FirebaseFirestore.getInstance()

        db.collection("UserV2")
            .document(authId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")

                    Log.d("ProfilePage", "First Name: $firstName, Last Name: $lastName")  // Log for verification

                    if (firstName != null && lastName != null) {
                        val fullName = "$firstName $lastName"
                        guestUserTextView.text = fullName
                    } else {
                        guestUserTextView.text = "Guest User"
                    }
                } else {
                    guestUserTextView.text = "Guest User" // If document doesn't exist
                    Log.d("ProfilePage", "Document does not exist for user ID: $authId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfilePage", "Error getting user details: ", exception)
                guestUserTextView.text = "Guest User" // If fetch fails, show "Guest User"
            }
    }
}
