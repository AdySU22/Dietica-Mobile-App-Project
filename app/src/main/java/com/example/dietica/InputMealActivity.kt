package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InputMealActivity : AppCompatActivity() {
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_input_meal)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        window.statusBarColor = ContextCompat.getColor(this, R.color.light_light_blue)

        functions = FirebaseFunctions.getInstance()

        // Decide whether to use the emulator
        if (false) {
            // Replace "10.0.2.2" with "localhost" if testing on a physical device
            functions.useEmulator("10.0.2.2", 5001)
            Log.d("FirebaseEmulator", "Using Firebase Emulator for Functions")
        }

        val btnBack: Button = findViewById(R.id.btnBack)
        val btnManualInput: Button = findViewById(R.id.btnManualInput)

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        btnManualInput.setOnClickListener {
            val intent = Intent(this, ManuallyInputMeal::class.java)
            startActivity(intent)
        }

        initHistory()
        initSearchFood()
    }

    private fun initHistory() {
        lifecycleScope.launch {
            try {
                val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                val authId = sharedPreferences.getString("authId", null)
                val data = mapOf(
                    "authId" to authId,
                    "page" to 0,
                    "limit" to 3
                )
                val result = functions.getHttpsCallable("getAllFood")
                    .call(data)
                    .await()
                showHistoryFood(result.data)
            } catch (e: Exception) {
                showHistoryFood(emptyList<Any>())
                Log.e("initHistory", "Error fetching history: ${e.message}", e)
                Toast.makeText(this@InputMealActivity, "Failed to load history", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showHistoryFood(data: Any?) {
        if (data == null) {
            Log.d("showSearchFood", "No data received")
            return
        }

        val foodList = data as? List<*> ?: return

        val historyContainer = findViewById<LinearLayout>(R.id.historyContainer)

        // Clear all existing child views before adding new ones
        historyContainer.removeAllViews()

        // Check if we have any food items
        if (foodList.isEmpty()) {
            Log.d("showSearchFood", "No food items found")

            val noResultView = LayoutInflater
                .from(this).inflate(R.layout.item_meal_not_found, historyContainer, false)
            historyContainer.addView(noResultView)

            return
        }

        val layoutParams = historyContainer.layoutParams
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        historyContainer.layoutParams = layoutParams

        // Iterate over the food list and create views dynamically
        foodList.forEach { foodItem ->
            val foodMap = foodItem as? Map<*, *> ?: return@forEach

            // Extract the necessary data from the foodMap
            val foodName = foodMap["name"] as? String ?: "Unknown"
            val foodCalories = (foodMap["calories"] as? Number)?.toInt() ?: 0
            val foodImageUrl = foodMap["food_url"] as? String ?: "" // Not used here, but we can add it if needed
            val foodId = foodMap["id"] as? String ?: ""

            // Inflate the item_meal layout
            val newLinearLayout = LayoutInflater.from(this@InputMealActivity)
                .inflate(R.layout.item_meal, historyContainer, false) as LinearLayout

            // Bind the views from item_meal
            val mealItemTitle = newLinearLayout.findViewById<TextView>(R.id.mealItemTitle)
            val mealItemSubtitle = newLinearLayout.findViewById<TextView>(R.id.mealItemSubtitle)
            val mealItemImage = newLinearLayout.findViewById<ImageView>(R.id.mealItemImage)

            // Set the data for the title and subtitle
            mealItemTitle.text = foodName
            mealItemSubtitle.text = "Calories: $foodCalories"  // Set subtitle to calories

            // Set a click listener on newLinearLayout
            newLinearLayout.setOnClickListener {
                // Create an intent to navigate to ManualInputActivity
                val intent = Intent(this@InputMealActivity, ManuallyInputMeal::class.java)
                // Pass the food_id to ManualInputActivity
                intent.putExtra("food_id", foodId)
                startActivity(intent)
            }

            // Add the new view to the container
            historyContainer.addView(newLinearLayout)
        }
    }

    private fun initSearchFood() {
        val inputSearchFood = findViewById<EditText>(R.id.searchFoodText)

        inputSearchFood.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                val searchText = inputSearchFood.text.toString()
                Log.d("initSearchFood", "Enter pressed$searchText")
                val data = mapOf(
                    "query" to searchText,
                    "page" to 0,
                    "limit" to 3
                )

                lifecycleScope.launch {
                    try {
                        val result = functions.getHttpsCallable("fatsecretSearch")
                            .call(data)
                            .await()
                        showSearchFood(result.data)
                    } catch (e: Exception) {
                        Log.e("showSearchFood", "Error fetching data: ${e.message}", e)
                        Toast.makeText(this@InputMealActivity, "Failed to fetch data. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun showSearchFood(data: Any?) {
        if (data == null) {
            Log.d("showSearchFood", "No data received")
            return
        }

        // Cast data to a Map if it's in that format
        val dataMap = data as? Map<*, *> ?: return

        // Navigate through the nested structure
        val foods = dataMap["foods"] as? Map<*, *>
        val foodList = foods?.get("food") as? List<*>

        // Find your result container and set it to wrap_content
        val resultContainer = findViewById<LinearLayout>(R.id.resultContainer)

        // Clear all existing child views before adding new ones
        resultContainer.removeAllViews()

        // Check if we have any food items
        if (foodList == null || foodList.isEmpty()) {
            Log.d("showSearchFood", "No food items found")

            val noResultView = LayoutInflater
                .from(this).inflate(R.layout.item_meal_not_found, resultContainer, false)
            resultContainer.addView(noResultView)

            return
        }

        val layoutParams = resultContainer.layoutParams
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        resultContainer.layoutParams = layoutParams

        // Iterate over the food list and create views dynamically
        foodList.forEach { foodItem ->
            val foodMap = foodItem as? Map<*, *> ?: return@forEach
            val foodName = foodMap["food_name"] as? String ?: "Unknown"
            val foodDescription = foodMap["food_description"] as? String ?: "No description available"
            val foodImageUrl = foodMap["food_url"] as? String ?: ""
            val foodId = foodMap["food_id"] as? String ?: ""

            // Inflate the item_meal layout
            val newLinearLayout = LayoutInflater.from(this)
                .inflate(R.layout.item_meal, resultContainer, false) as LinearLayout

            // Bind the views from item_meal
            val mealItemTitle = newLinearLayout.findViewById<TextView>(R.id.mealItemTitle)
            val mealItemSubtitle = newLinearLayout.findViewById<TextView>(R.id.mealItemSubtitle)
            val mealItemImage = newLinearLayout.findViewById<ImageView>(R.id.mealItemImage)

            // Set the data
            mealItemTitle.text = foodName
            mealItemSubtitle.text = foodDescription

            // Set a click listener on newLinearLayout
            newLinearLayout.setOnClickListener {
                // Create an intent to navigate to ManualInputActivity
                val intent = Intent(this, ManuallyInputMeal::class.java)
                // Pass the food_id to ManualInputActivity
                intent.putExtra("fatsecret_id", foodId)
                startActivity(intent)
            }

            // Add the new view to the container
            resultContainer.addView(newLinearLayout)
        }
    }
}