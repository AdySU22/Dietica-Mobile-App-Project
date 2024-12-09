package com.example.dietica

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.dietica.services.LoadingUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeActivity : BaseActivity() {
    private lateinit var waterCountTextView: TextView
    private lateinit var inputMealTextView: TextView
    private lateinit var profileButton: Button
    private lateinit var btnNotification: ImageView
    private lateinit var btnGo: Button
    private lateinit var exercisePlus: ImageView
    private lateinit var weightPlus: ImageView
    /*private lateinit var buttonBodyComposition: Button*/
    private lateinit var homeLinearLayout: LinearLayout
    private lateinit var exerciseFrameLayout: FrameLayout
    private lateinit var calorieInputLayout: ConstraintLayout
    private lateinit var reportLinearLayout: LinearLayout
    private lateinit var toDoFrameLayout: FrameLayout
    private lateinit var addWaterText: EditText
    private lateinit var functions: FirebaseFunctions
    private lateinit var sharedPreferences: SharedPreferences
    private var totalWaterIntake = 0 // Store total water intake in ml
    private val dailyWaterGoal = 2000 // Daily water goal in ml

    private lateinit var progressOverlay: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val dotsIndicator = findViewById<DotsIndicator>(R.id.dotsIndicator)

        val progressValues = listOf(
            listOf(30, 31, 32),
            listOf(60, 61, 62),
            listOf(90, 91, 92)
        )
        val layoutTypes = listOf(
            SliderAdapter.LAYOUT_TYPE_1,
            SliderAdapter.LAYOUT_TYPE_2,
            SliderAdapter.LAYOUT_TYPE_3
        )
        val adapter = SliderAdapter(progressValues, layoutTypes)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3 // Adjust the number as needed

        dotsIndicator.attachTo(viewPager)

        // Initialize Firebase Functions
        functions = Firebase.functions

        // Initialize loading
        progressOverlay = findViewById(R.id.progress_overlay)

        sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
        val authId = sharedPreferences.getString("authId", null)
        Log.d("HomeActivity", authId ?: "Token is null")

        // Get today food summaries
        initTodayFoodSummary()

        // Check if the water intake needs to be reset
        resetWaterIntakeIfNewDay()

        // Setting up the UI elements
        waterCountTextView = findViewById(R.id.waterCount)
        inputMealTextView = findViewById(R.id.inputMealText)
        profileButton = findViewById(R.id.profileButton)
        btnNotification = findViewById(R.id.btnNotification)
        btnGo = findViewById(R.id.btnGo)
        exercisePlus = findViewById(R.id.exercisePlus)
        weightPlus = findViewById(R.id.weightPlus)
       /* buttonBodyComposition = findViewById(R.id.buttonBodyComposition)*/
        homeLinearLayout = findViewById(R.id.homeLinearLayout)
        exerciseFrameLayout = findViewById(R.id.exerciseFrameLayout)
        calorieInputLayout = findViewById(R.id.calorieInputLayout)
        reportLinearLayout = findViewById(R.id.reportLinearLayout)
        toDoFrameLayout = findViewById(R.id.toDoFrameLayout)
        addWaterText = findViewById(R.id.addWaterText)

        // Load saved water intake amount
        totalWaterIntake = sharedPreferences.getInt("totalWaterIntake", 0)
        updateWaterCount(totalWaterIntake)

        initWaterInput()
//        addWaterText.setOnClickListener {
//            val input = EditText(this)
//            input.hint = "Enter amount in ml"
//
//            AlertDialog.Builder(this)
//                .setTitle("Log Water Intake")
//                .setMessage("Enter the amount of water (in ml):")
//                .setView(input)
//                .setPositiveButton("OK") { _, _ ->
//                    val amount = input.text.toString().toIntOrNull()
//                    if (amount != null && amount > 0) {
//                        logWaterIntake(amount)
//                    } else {
//                        Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }
//                .setNegativeButton("Cancel", null)
//                .show()
//        }

        inputMealTextView.setOnClickListener {
            val intent = Intent(this, InputMealActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val authId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("HomeActivity", "Auth ID: $authId")  // Check the value of authId
            val intent = Intent(this, ProfilePageActivity::class.java)
            if (authId != null) {
                intent.putExtra("authId", authId)
                Log.d("HomeActivity", "Passing Auth ID: $authId")  // Log the passed authId
            } else {
                Log.d("HomeActivity", "User not authenticated")
            }
            startActivity(intent)
        }

        btnNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        btnGo.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
            startActivity(intent)
        }

        exercisePlus.setOnClickListener {
            val authId = FirebaseAuth.getInstance().currentUser?.uid
            val intent = Intent(this, MyExerciseActivity::class.java)
            if (authId != null) {
                intent.putExtra("authId", authId)
                Log.d("HomeActivity", "Passing Auth ID: $authId")  // Log the passed authId
            } else {
                Log.d("HomeActivity", "User not authenticated")
            }
            startActivity(intent)
        }

        weightPlus.setOnClickListener {
            val intent = Intent(this, WeightTargetActivity::class.java)
            startActivity(intent)
        }

        /*buttonBodyComposition.setOnClickListener {
            val intent = Intent(this, HealthReportActivity::class.java)
            startActivity(intent)
        }*/

        homeLinearLayout.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        exerciseFrameLayout.setOnClickListener {
            val intent = Intent(this, MyExerciseActivity::class.java)
            startActivity(intent)
        }

        calorieInputLayout.setOnClickListener {
            val intent = Intent(this, InputMealActivity::class.java)
            startActivity(intent)
        }

        reportLinearLayout.setOnClickListener {
            val intent = Intent(this, HealthReportActivity::class.java)
            startActivity(intent)
        }

        toDoFrameLayout.setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initTodayFoodSummary() {
        val authId = sharedPreferences.getString("authId", null)
        val iataTimeZone = TimeZone.getDefault().id
        if (authId != null) {
            val data = mapOf(
                "authId" to authId,
                "iataTimeZone" to iataTimeZone
            )

            // Start loading overlay
            LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
            // Call the Cloud Function to get today's food summary
            functions.getHttpsCallable("homeFoodTodaySummary")
                .call(data)
                .addOnSuccessListener { result ->
                    // Extract data from the result
                    val summary = result.data as? Map<String, Any>

                    // Extract and display the summary values
                    if (summary != null) {
                        val totalCalories = (summary["totalCalories"] as? Number)?.toInt() ?: 0
                        val totalCarbs = (summary["totalCarbs"] as? Number)?.toInt() ?: 0
                        val totalFat = (summary["totalFat"] as? Number)?.toInt() ?: 0
                        val totalProtein = (summary["totalProtein"] as? Number)?.toInt() ?: 0
                        val totalSugar = (summary["totalSugar"] as? Number)?.toInt() ?: 0
                        val totalSodium = (summary["totalSodium"] as? Number)?.toInt() ?: 0
                        val totalCholesterol = (summary["totalCholesterol"] as? Number)?.toInt() ?: 0
                        val totalFiber = (summary["totalFiber"] as? Number)?.toInt() ?: 0

                        // Define max values for each nutrient
                        val maxCarbs = 250  // Maximum carbs in grams
                        val maxFat = 100    // Maximum fat in grams
                        val maxProtein = 120 // Maximum protein in grams
                        val maxSugar = 100  // Maximum sugar in grams
                        val maxSodium = 1000 // Maximum sodium in milligrams
                        val maxCholesterol = 300 // Maximum cholesterol in milligrams
                        val maxFiber = 50    // Maximum fiber in grams

                        // Update the text UI with the retrieved summary
                        findViewById<TextView>(R.id.today_total).text = "$totalCalories"
                        findViewById<TextView>(R.id.progressBarCarbsText).text = "$totalCarbs/$maxCarbs" + "g"
                        findViewById<TextView>(R.id.progressBarFatText).text = "$totalFat/$maxFat" + "g"
                        findViewById<TextView>(R.id.progressBarProteinText).text = "$totalProtein/$maxProtein" + "g"
                        findViewById<TextView>(R.id.progressBarSugarText).text = "$totalSugar g"
                        findViewById<TextView>(R.id.progressBarSodiumText).text = "$totalSodium mg"
                        findViewById<TextView>(R.id.progressBarCholesterolText).text = "$totalCholesterol mg"
                        findViewById<TextView>(R.id.progressBarFiberText).text = "$totalFiber g"

                        // Calculate progress percentages
                        val carbsProgress = ((totalCarbs.toFloat() / maxCarbs) * 100).toInt()
                        val fatProgress = ((totalFat.toFloat() / maxFat) * 100).toInt()
                        val proteinProgress = ((totalProtein.toFloat() / maxProtein) * 100).toInt()
                        val sugarProgress = ((totalSugar.toFloat() / maxSugar) * 100).toInt()
                        val sodiumProgress = ((totalSodium.toFloat() / maxSodium) * 100).toInt()
                        val cholesterolProgress = ((totalCholesterol.toFloat() / maxCholesterol) * 100).toInt()
                        val fiberProgress = ((totalFiber.toFloat() / maxFiber) * 100).toInt()

                        // Update the progress bars
                        findViewById<ProgressBar>(R.id.progressBarCarbs).progress = carbsProgress
                        findViewById<ProgressBar>(R.id.progressBarFat).progress = fatProgress
                        findViewById<ProgressBar>(R.id.progressBarProtein).progress = proteinProgress
                        findViewById<ProgressBar>(R.id.progressBarSugar).progress = sugarProgress
                        findViewById<ProgressBar>(R.id.progressBarSodium).progress = sodiumProgress
                        findViewById<ProgressBar>(R.id.progressBarCholesterol).progress = cholesterolProgress
                        findViewById<ProgressBar>(R.id.progressBarFiber).progress = fiberProgress

                        // Update BMI
                        val bmiValue = (summary["bmi"] as? Number)?.toDouble() ?: 0.0
                        findViewById<TextView>(R.id.bmiNumberText).text = String.format("%.2f", bmiValue)

                        // Update BMI Category
                        val bmiCategoryTextView = findViewById<TextView>(R.id.bmiCategoryText)
                        val bmiCategory = summary["bmiCategory"] as? String ?: "Unknown"
                        bmiCategoryTextView.text = bmiCategory
                        // Set the color based on category
                        val categoryColor = when (bmiCategory) {
                            "Underweight" -> Color.parseColor("#FFA07A") // Light Salmon
                            "Normal" -> Color.BLACK
                            "Overweight" -> Color.parseColor("#FFD700") // Gold
                            "Obese" -> Color.parseColor("#FF4500") // Orange Red
                            else -> Color.GRAY
                        }
                        bmiCategoryTextView.setTextColor(categoryColor)

                        // Update BMI last updated at
                        val bmiUpdatedAt = summary["bmiUpdatedAt"]
                        if (bmiUpdatedAt is Map<*, *>) {
                            val seconds = (bmiUpdatedAt["_seconds"] as? Number)?.toLong()
                            if (seconds != null) {
                                val formattedDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                                    .format(Date(seconds * 1000))
                                findViewById<TextView>(R.id.lastUpdatedText).text = "Last updated at $formattedDate"
                            } else {
                                Log.d("HomeActivity", "Failed to retrieve seconds from bmiUpdatedAt")
                            }
                        } else {
                            Log.d("HomeActivity", "bmiUpdatedAt is not a Map, value: $bmiUpdatedAt")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors that occur during the call
                    Log.e("FoodSummary", "Error fetching today's food summary", exception)
                }
                .addOnCompleteListener {
                    // Hide loading overlay
                    LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)
                }
        }
    }


    private fun resetWaterIntakeIfNewDay() {
        // Get the current date
        val currentDate = System.currentTimeMillis()
        val lastResetTimestamp = sharedPreferences.getLong("lastResetTimestamp", 0)

        // If last reset timestamp is not from today, reset the water intake
        if (lastResetTimestamp != getCurrentDayTimestamp(currentDate)) {
            totalWaterIntake = 0
            sharedPreferences.edit().putInt("totalWaterIntake", totalWaterIntake).apply()
            sharedPreferences.edit()
                .putLong("lastResetTimestamp", getCurrentDayTimestamp(currentDate)).apply()
        }
    }

    private fun getCurrentDayTimestamp(currentTimeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun initWaterInput() {
        val addWater = findViewById<EditText>(R.id.addWaterText)

        // Initial underscores
        val placeholderText = "___________"

        // Set the placeholder initially
        addWater.setText(placeholderText)

        // Monitor text changes
        addWater.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Remove the placeholder if user starts typing
                if (s?.toString() == placeholderText) {
                    addWater.setText("")
                }
            }
        })

        // Monitor focus changes
        addWater.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Remove placeholder when focused and empty
                if (addWater.text.toString() == placeholderText) {
                    addWater.setText("")
                }
            } else {
                // Reapply placeholder if empty when losing focus
                if (addWater.text.toString().isEmpty()) {
                    addWater.setText(placeholderText)
                }
            }
        }

        val btnAddWater = findViewById<Button>(R.id.btnAddWater)
        btnAddWater.setOnClickListener {
            val amount = addWater.text.toString().toIntOrNull()
            if (amount != null && amount > 0) {
                logWaterIntake(amount)
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logWaterIntake(amount: Int) {
        val btnAddWater = findViewById<Button>(R.id.btnAddWater)
        val authId = sharedPreferences.getString("authId", null)

        if (authId != null) {
            btnAddWater.isEnabled = false
            btnAddWater.alpha = 0.5f

            val data = hashMapOf(
                "authId" to authId,
                "amount" to amount
            )

            functions
                .getHttpsCallable("setWaterLog")
                .call(data)
                .addOnSuccessListener { result ->
                    val message = result.data as Map<*, *>
                    Log.d("HomeActivity", "Water log created: ${message["message"]}")

                    // Update the local total and UI for water intake
                    totalWaterIntake += amount
                    updateWaterCount(totalWaterIntake)

                    // Save the updated totalWaterIntake to SharedPreferences
                    saveWaterIntake(totalWaterIntake)

                    btnAddWater.isEnabled = true
                    btnAddWater.alpha = 1.0f
                }
                .addOnFailureListener { e ->
                    Log.e("HomeActivity", "Failed to log water intake", e)
                }
        } else {
            Log.e("HomeActivity", "authId is null")
        }
    }

    private fun updateWaterCount(total: Int) {
        // Update water intake text in the format "current/goal ml"
        waterCountTextView.text = "$total/$dailyWaterGoal ml"
    }

    private fun saveWaterIntake(total: Int) {
        // Save total water intake to SharedPreferences
        sharedPreferences.edit().putInt("totalWaterIntake", total).apply()
    }
}