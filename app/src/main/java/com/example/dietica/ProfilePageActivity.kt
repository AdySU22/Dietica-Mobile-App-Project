package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dietica.services.LoadingUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePageActivity : BaseActivity() {

    private lateinit var guestUserTextView: TextView

    private lateinit var progressOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_page)

        // Initialize the TextView
        guestUserTextView = findViewById(R.id.guestUserTextView)

        // Initialize loading
        progressOverlay = findViewById(R.id.progress_overlay)

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

        btnBack.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            val authId = FirebaseAuth.getInstance().currentUser?.uid
            if (authId != null) {
                intent.putExtra("authId", authId)
            }
            startActivity(intent)
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
        val db = FirebaseFirestore.getInstance()
        val profileImageView: ImageView = findViewById(R.id.profileImage)

        // Start loading overlay
        LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
        db.collection("UserV2")
            .document(authId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profileImageUrl = document.getString("profileImageUrl")
                    val firstName = document.getString("firstName") ?: "Guest"
                    val lastName = document.getString("lastName") ?: ""

                    guestUserTextView.text = "$firstName $lastName"

                    // Load profile image using Glide
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .placeholder(R.drawable.profile_picture) // Optional placeholder
                            .circleCrop() // Makes the image circular
                            .into(profileImageView)
                    }
                } else {
                    guestUserTextView.text = "Guest User"
                    Log.d("ProfilePage", "Document does not exist.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfilePage", "Error fetching user details", exception)
                guestUserTextView.text = "Guest User"
            }
            .addOnCompleteListener {
                // Hide loading overlay
                LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)
            }
    }
}
