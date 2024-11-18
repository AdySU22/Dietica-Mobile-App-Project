package com.example.dietica

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import java.text.SimpleDateFormat
import java.util.*

class EditProfile : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var authId: String? = null
    private var selectedActivityLevel: String = "Sedentary" // Default activity level

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Check for authentication
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        authId = user.uid // Set the authId from the FirebaseAuth instance

        // Find views
        val btnEditProfilePicture: FrameLayout = findViewById(R.id.btnEditProfilePicture)
        val guestUserText: TextView = findViewById(R.id.guestUserText)
        val genderText: TextView = findViewById(R.id.genderText)
        val heightText: TextView = findViewById(R.id.heightText)
        val weightText: TextView = findViewById(R.id.weightText)
        val birthDateText: TextView = findViewById(R.id.birtDateText)
        val btnCancel: Button = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        // Activity level views
        val icon1: ImageView = findViewById(R.id.icon1)
        val icon2: ImageView = findViewById(R.id.icon2)
        val icon3: ImageView = findViewById(R.id.icon3)
        val icon4: ImageView = findViewById(R.id.icon4)
        val headerText: TextView = findViewById(R.id.headerText)
        val subheaderText: TextView = findViewById(R.id.subheaderText)
        val textContainer: View = findViewById(R.id.textContainer)

        // Click listener for activity icons
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
                    subheaderText.text = "Typical daily activity with 30–60 minutes of exercise. E.g., walking 5km."
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

        // Assign click listeners to activity icons
        icon1.setOnClickListener(iconClickListener)
        icon2.setOnClickListener(iconClickListener)
        icon3.setOnClickListener(iconClickListener)
        icon4.setOnClickListener(iconClickListener)

        // Other existing listeners and functions
        btnEditProfilePicture.setOnClickListener {
            openImageChooser()
        }

        // Fetch and display the user's profile data from Firestore
        fetchProfileData()

        guestUserText.setOnClickListener { showNameEditDialog(guestUserText) }
        genderText.setOnClickListener { showGenderPicker(genderText) }
        heightText.setOnClickListener { showNumberPicker(heightText, "Select Height", 100, 250) }
        weightText.setOnClickListener { showNumberPicker(weightText, "Select Weight", 30, 150) }
        birthDateText.setOnClickListener { showDatePicker(birthDateText) }

        btnSave.setOnClickListener {
            saveProfileData(guestUserText, genderText, heightText, weightText, birthDateText)
        }

        btnCancel.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchProfileData() {
        if (authId.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("ProfileFetch", "Fetching profile for user with ID: $authId")

        val functions = FirebaseFunctions.getInstance()

        functions
            .getHttpsCallable("getProfileV2")
            .call(mapOf("authId" to authId))
            .addOnSuccessListener { result ->
                val data = result.data as? Map<*, *> ?: return@addOnSuccessListener
                Log.d("ProfileFetch", "Profile data: $data")

                val firstName = data["firstName"]?.toString() ?: "Guest"
                val lastName = data["lastName"]?.toString() ?: "Not Specified"

                // Get the birthdate from Firestore (it should be a Timestamp object)
                val birthdateField = data["birthdate"] as? Timestamp
                if (birthdateField != null) {
                    val birthDate = birthdateField.toDate()  // Convert Timestamp to Date object
                    val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())  // Format the date
                    val birthDateStr = sdf.format(birthDate)
                    findViewById<TextView>(R.id.birtDateText).text = birthDateStr  // Display the formatted date
                }

                // Update UI with the fetched profile data
                findViewById<TextView>(R.id.guestUserText).text = "$firstName $lastName"
                findViewById<TextView>(R.id.genderText).text = data["gender"]?.toString() ?: "Not Specified"
                findViewById<TextView>(R.id.heightText).text = "${data["height"]} cm"
                findViewById<TextView>(R.id.weightText).text = "${data["weight"]} kg"

                // Set activity level
                setActivityLevel(data["activityLevels"]?.toString() ?: "Sedentary")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch profile data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
        icon1.setImageResource(R.drawable.icon_sedentary)
        icon2.setImageResource(R.drawable.icon_light)
        icon3.setImageResource(R.drawable.icon_moderate)
        icon4.setImageResource(R.drawable.icon_active)

        // Reset the border or background if needed (optional)
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
                selectedActivityLevel = "Sedentary"  // Update the selected activity level
            }
            "Light Activity" -> {
                headerText.text = "Light Activity"
                subheaderText.text = "Typical daily activity with 30–60 minutes of exercise. E.g., walking 5km."
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
        val heightStr = heightTextView.text.toString().trim()
        val weightStr = weightTextView.text.toString().trim()
        val gender = genderTextView.text.toString().trim()
        val birthDate = birthDateTextView.text.toString().trim()

        // Check for empty fields
        if (name.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || gender.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val height = parseHeightOrWeight(heightStr, "height")
        val weight = parseHeightOrWeight(weightStr, "weight")

        if (height == null || weight == null) {
            Toast.makeText(this, "Invalid height or weight value.", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse the birthdate into a Timestamp object
        val birthDateTimestamp = parseBirthdate(birthDate)

        // Split the name into first and last names
        val nameParts = name.split(" ")
        val firstName = nameParts.getOrNull(0) ?: "FirstName"
        val lastName = if (nameParts.size > 1) nameParts[1] else "LastName"

        // Prepare data to save in Firestore
        val updates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "gender" to gender,
            "height" to height,
            "weight" to weight,
            "birthdate" to birthDateTimestamp,  // Save the Timestamp object
            "activityLevels" to selectedActivityLevel,
            "illnesses" to "test"
        )

        Log.d("ProfileSave", "Updating profile with data: $updates")

        firestore.collection("UserV2").document(authId!!)
            .set(updates, SetOptions.merge())  // Merge to avoid overwriting entire document
            .addOnSuccessListener {
                Log.d("ProfileSave", "Profile updated successfully!")
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileSave", "Failed to update profile: ${e.localizedMessage}")
                Toast.makeText(this, "Failed to update profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun parseBirthdate(birthDateStr: String): Timestamp {
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())  // Use the same format you display
        val date = sdf.parse(birthDateStr) ?: Date()  // Default to current date if parsing fails
        return Timestamp(date)
    }

    // Helper method to parse height and weight
    private fun parseHeightOrWeight(value: String, type: String): Float? {
        // Strip out any non-numeric characters (e.g., cm, kg, etc.)
        val cleanValue = value.replace("[^0-9.]".toRegex(), "")

        // Try to convert the cleaned string to a Float
        return try {
            cleanValue.toFloat()
        } catch (e: NumberFormatException) {
            // If conversion fails, return null
            null
        }
    }

    private fun showNameEditDialog(guestUserText: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Name")
        val input = EditText(this)
        input.setText(guestUserText.text.toString())
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            guestUserText.text = input.text.toString()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showGenderPicker(genderText: TextView) {
        val genders = arrayOf("Male", "Female")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Gender")
        builder.setItems(genders) { _, which ->
            genderText.text = genders[which]
        }
        builder.show()
    }

    private fun showNumberPicker(textView: TextView, title: String, min: Int, max: Int) {
        val builder = AlertDialog.Builder(this)
        val numberPicker = NumberPicker(this)
        numberPicker.minValue = min
        numberPicker.maxValue = max
        numberPicker.value = min
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

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
}
