package com.example.dietica

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dietica.services.LoadingUtils
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class EditProfile : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var authId: String? = null
    private var selectedImageUri: Uri? = null
    private var selectedActivityLevel: String = "Sedentary"

    private lateinit var progressOverlay: View

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firestore and Firebase Storage
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize loading
        progressOverlay = findViewById(R.id.progress_overlay)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        authId = user.uid

        val authId = intent.getStringExtra("authId")
        if (authId == null) {
            Toast.makeText(this, "No user ID provided. Returning to profile page.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("EditProfile", "Received authId: $authId")

        // Views
        val btnEditProfilePicture: FrameLayout = findViewById(R.id.btnEditProfilePicture)
        val profileImageView: ImageView = findViewById(R.id.profileImage)
        val guestUserText: TextView = findViewById(R.id.guestUserText)
        val genderText: TextView = findViewById(R.id.genderText)
        val heightText: TextView = findViewById(R.id.heightText)
        val weightText: TextView = findViewById(R.id.weightText)
        val birthDateText: TextView = findViewById(R.id.birtDateText)
        val btnCancel: Button = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        // Activity Level Views
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)
        val headerText: TextView = findViewById(R.id.headerText)
        val subheaderText: TextView = findViewById(R.id.subheaderText)
        val textContainer: View = findViewById(R.id.textContainer)

        // Activity Level Selection
        val iconClickListener = View.OnClickListener { view ->
            textContainer.visibility = View.VISIBLE
            when (view.id) {
                R.id.icon1 -> {
                    headerText.text = "Sedentary"
                    subheaderText.text = "Typical daily activities. Measures basal metabolic rate."
                    selectedActivityLevel = "Sedentary"
                }
                R.id.icon2 -> {
                    headerText.text = "Light Activity"
                    subheaderText.text = "Typical daily activity with 30–60 minutes of exercise."
                    selectedActivityLevel = "Light Activity"
                }
                R.id.icon3 -> {
                    headerText.text = "Active"
                    subheaderText.text = "Typical daily activity with at least 60 minutes of exercise."
                    selectedActivityLevel = "Active"
                }
                R.id.icon4 -> {
                    headerText.text = "Very Active"
                    subheaderText.text = "Typical daily activity with at least 60 minutes of heavy exercise."
                    selectedActivityLevel = "Very Active"
                }
            }
        }
        icon1.setOnClickListener(iconClickListener)
        icon2.setOnClickListener(iconClickListener)
        icon3.setOnClickListener(iconClickListener)
        icon4.setOnClickListener(iconClickListener)

        // Set up button actions
        btnEditProfilePicture.setOnClickListener { openImageChooser() }
        guestUserText.setOnClickListener { showNameEditDialog(guestUserText) }
        genderText.setOnClickListener { showGenderPicker(genderText) }
        heightText.setOnClickListener { showNumberPicker(heightText, "Select Height", 100, 250) }
        weightText.setOnClickListener { showNumberPicker(weightText, "Select Weight", 30, 150) }
        birthDateText.setOnClickListener { showDatePicker(birthDateText) }

        btnSave.setOnClickListener {
            saveProfileData(guestUserText, genderText, heightText, weightText, birthDateText)
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }
        btnCancel.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }

        fetchProfileData(profileImageView)
    }

    private fun fetchProfileData(profileImageView: ImageView) {
        if (authId.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Start loading overlay
        LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)

        firestore.collection("UserV2").document(authId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileImageUrl = document.getString("profileImageUrl")
                    val firstName = document.getString("firstName") ?: "Guest"
                    val lastName = document.getString("lastName") ?: ""
                    val gender = document.getString("gender") ?: "Not Specified"
                    val height = document.getDouble("height") ?: 0.0
                    val weight = document.getDouble("weight") ?: 0.0
                    val birthDateTimestamp = document.getTimestamp("birthdate")
                    val activityLevel = document.getString("activityLevels") ?: "Sedentary"

                    // Load profile image
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .placeholder(R.drawable.profile_picture) // Optional placeholder
                            .circleCrop() // Ensures circular cropping
                            .into(profileImageView)
                    }

                    // Set fetched data
                    findViewById<TextView>(R.id.guestUserText).text = "$firstName $lastName"
                    findViewById<TextView>(R.id.genderText).text = gender
                    findViewById<TextView>(R.id.heightText).text = "$height cm"
                    findViewById<TextView>(R.id.weightText).text = "$weight kg"
                    birthDateTimestamp?.toDate()?.let { date ->
                        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                        findViewById<TextView>(R.id.birtDateText).text = sdf.format(date)
                    }

                    // Set activity level
                    setActivityLevel(activityLevel)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                // Hide loading overlay
                LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)
            }
    }

    private fun setActivityLevel(activityLevel: String) {
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)
        val headerText: TextView = findViewById(R.id.headerText)
        val subheaderText: TextView = findViewById(R.id.subheaderText)
        val textContainer: View = findViewById(R.id.textContainer)

        // Default all icons to unselected state
        icon1.setBackgroundResource(R.drawable.circular_activity_level_border)
        icon2.setBackgroundResource(R.drawable.circular_activity_level_border)
        icon3.setBackgroundResource(R.drawable.circular_activity_level_border)
        icon4.setBackgroundResource(R.drawable.circular_activity_level_border)

        // Highlight the selected activity level icon
        when (activityLevel) {
            "Sedentary" -> {
                headerText.text = "Sedentary"
                subheaderText.text = "Typical daily activities. Measures basal metabolic rate."
                icon1.setBackgroundResource(R.drawable.circular_activity_level_border_selected)
                selectedActivityLevel = "Sedentary"
            }
            "Light Activity" -> {
                headerText.text = "Light Activity"
                subheaderText.text = "Typical daily activity with 30–60 minutes of exercise."
                icon2.setBackgroundResource(R.drawable.circular_activity_level_border_selected)
                selectedActivityLevel = "Light Activity"
            }
            "Active" -> {
                headerText.text = "Active"
                subheaderText.text = "Typical daily activity with at least 60 minutes of exercise."
                icon3.setBackgroundResource(R.drawable.circular_activity_level_border_selected)
                selectedActivityLevel = "Active"
            }
            "Very Active" -> {
                headerText.text = "Very Active"
                subheaderText.text = "Typical daily activity with at least 60 minutes of heavy exercise."
                icon4.setBackgroundResource(R.drawable.circular_activity_level_border_selected)
                selectedActivityLevel = "Very Active"
            }
            else -> {
                headerText.text = "Unknown"
                subheaderText.text = "No activity level selected."
            }
        }

        // Make the text container visible
        textContainer.visibility = View.VISIBLE
    }


    private fun saveProfileData(
        nameTextView: TextView,
        genderTextView: TextView,
        heightTextView: TextView,
        weightTextView: TextView,
        birthDateTextView: TextView
    ) {
        val name = nameTextView.text.toString().trim()
        val gender = genderTextView.text.toString().trim()
        val heightStr = heightTextView.text.toString().trim()
        val weightStr = weightTextView.text.toString().trim()
        val birthDateStr = birthDateTextView.text.toString().trim()

        if (name.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || gender.isEmpty() || birthDateStr.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val height = heightStr.replace(" cm", "").toFloatOrNull()
        val weight = weightStr.replace(" kg", "").toFloatOrNull()
        val birthDateTimestamp = SimpleDateFormat("d/M/yyyy", Locale.getDefault()).parse(birthDateStr)?.let { Timestamp(it) }

        if (height == null || weight == null || birthDateTimestamp == null) {
            Toast.makeText(this, "Invalid height, weight, or birthdate.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf(
            "firstName" to name.split(" ").first(),
            "lastName" to name.split(" ").getOrElse(1) { "" },
            "gender" to gender,
            "height" to height,
            "weight" to weight,
            "birthdate" to birthDateTimestamp,
            "activityLevels" to selectedActivityLevel
        )

        selectedImageUri?.let { uri ->
            uploadProfileImage(uri) { imageUrl ->
                updates["profileImageUrl"] = imageUrl
                updateFirestoreProfile(updates)
            }
        } ?: updateFirestoreProfile(updates)
    }

    private fun uploadProfileImage(uri: Uri, onComplete: (String) -> Unit) {
        val storageRef = storage.reference.child("profile_images/$authId.jpg")
        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onComplete(downloadUrl.toString())
                    Log.d("EditProfile", "Image uploaded successfully: $downloadUrl")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload image: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("EditProfile", "Image upload failed", exception)
            }
    }

    private fun updateFirestoreProfile(updates: Map<String, Any>) {
        firestore.collection("UserV2").document(authId!!)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            val profileImageView: ShapeableImageView = findViewById(R.id.profileImage)
            profileImageView.setImageURI(selectedImageUri) // Show selected image temporarily
        }
    }


    private fun showNameEditDialog(nameTextView: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Name")
        val input = EditText(this)
        input.setText(nameTextView.text.toString())
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ -> nameTextView.text = input.text.toString() }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showGenderPicker(genderText: TextView) {
        val genders = arrayOf("Male", "Female")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Gender")
        builder.setItems(genders) { _, which -> genderText.text = genders[which] }
        builder.show()
    }

    private fun showNumberPicker(textView: TextView, title: String, min: Int, max: Int) {
        val builder = AlertDialog.Builder(this)
        val numberPicker = NumberPicker(this)
        numberPicker.minValue = min
        numberPicker.maxValue = max
        builder.setTitle(title)
        builder.setView(numberPicker)
        builder.setPositiveButton("OK") { _, _ ->
            textView.text = "${numberPicker.value} ${if (title.contains("Height")) "cm" else "kg"}"
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showDatePicker(birthDateText: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                birthDateText.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}
