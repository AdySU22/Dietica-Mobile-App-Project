package com.example.dietica

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ManuallyInputMeal : AppCompatActivity() {
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manually_input_meal)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        functions = FirebaseFunctions.getInstance()

        // Decide whether to use the emulator
        if (false) {
            // Replace "10.0.2.2" with "localhost" if testing on a physical device
            functions.useEmulator("10.0.2.2", 5001)
            Log.d("FirebaseEmulator", "Using Firebase Emulator for Functions")
        }

        initFatsecretData()
        initFoodData()
        initBtnCancel()

        val btnBack: Button = findViewById(R.id.btnBack)
        val btnSave: Button = findViewById(R.id.btnSave)

        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = createFood()
                    Log.d("FoodCreationResult", "Result: ${result.data}")
                    Toast.makeText(this@ManuallyInputMeal, "Food created successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity
                } catch (e: Exception) {
                    Log.e("FoodCreationError", "Error creating food: ${e.message}")
                    Toast.makeText(this@ManuallyInputMeal, "Failed to create food: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun initBtnCancel() {
        val inputFoodName: EditText = findViewById(R.id.inputFoodName)
        val inputServingType: EditText = findViewById(R.id.inputServingType)
        val inputCalories: EditText = findViewById(R.id.inputCalories)
        val inputFats: EditText = findViewById(R.id.inputFats)
        val inputSaturatedFat: EditText = findViewById(R.id.inputSaturatedFat)
        val inputUnsaturatedFat: EditText = findViewById(R.id.inputUnsaturatedFat)
        val inputTransFat: EditText = findViewById(R.id.inputTransFat)
        val inputCholesterol: EditText = findViewById(R.id.inputCholesterol)
        val inputSodium: EditText = findViewById(R.id.inputSodium)
        val inputCarbohydrates: EditText = findViewById(R.id.inputCarbohydrates)
        val inputProtein: EditText = findViewById(R.id.inputProtein)
        val inputSugar: EditText = findViewById(R.id.inputSugar)
        val inputFiber: EditText = findViewById(R.id.inputFiber)
        val btnCancel: Button = findViewById(R.id.btnCancel)

        // Cancel Button Click Listener
        btnCancel.setOnClickListener {
            clearFields(
                inputFoodName, inputServingType, inputCalories, inputFats,
                inputSaturatedFat, inputUnsaturatedFat, inputTransFat,
                inputCholesterol, inputSodium, inputCarbohydrates,
                inputProtein, inputSugar, inputFiber
            )
        }
    }

    private fun clearFields(vararg fields: EditText) {
        for (field in fields) {
            field.text.clear()
        }
    }

    private suspend fun createFood(): HttpsCallableResult {
        val inputFoodName = findViewById<EditText>(R.id.inputFoodName).text.toString()
        val inputServingType = findViewById<EditText>(R.id.inputServingType).text.toString()
        val inputCalories = findViewById<EditText>(R.id.inputCalories).text.toString().toInt()
        val inputFats = findViewById<EditText>(R.id.inputFats).text.toString().toInt()
        val inputSaturatedFat = findViewById<EditText>(R.id.inputSaturatedFat).text.toString().toInt()
        val inputUnsaturatedFat = findViewById<EditText>(R.id.inputUnsaturatedFat).text.toString().toInt()
        val inputTransFat = findViewById<EditText>(R.id.inputTransFat).text.toString().toInt()
        val inputCholesterol = findViewById<EditText>(R.id.inputCholesterol).text.toString().toInt()
        val inputSodium = findViewById<EditText>(R.id.inputSodium).text.toString().toInt()
        val inputCarbohydrates = findViewById<EditText>(R.id.inputCarbohydrates).text.toString().toInt()
        val inputProtein = findViewById<EditText>(R.id.inputProtein).text.toString().toInt()
        val inputSugar = findViewById<EditText>(R.id.inputSugar).text.toString().toInt()
        val inputFiber = findViewById<EditText>(R.id.inputFiber).text.toString().toInt()

        val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
        val authId = sharedPreferences.getString("authId", null)

        val data = mapOf(
            "authId" to authId,
            "food" to mapOf(
                "name" to inputFoodName,
                "servingType" to inputServingType,
                "calories" to inputCalories,
                "fat" to inputFats,
                "saturatedFat" to inputSaturatedFat,
                "unsaturatedFat" to inputUnsaturatedFat,
                "transFat" to inputTransFat,
                "cholesterol" to inputCholesterol,
                "sodium" to inputSodium,
                "carbs" to inputCarbohydrates,
                "protein" to inputProtein,
                "sugar" to inputSugar,
                "fiber" to inputFiber
            )
        )

        val result = functions.getHttpsCallable("createFood")
            .call(data)
            .await()
        return result
    }

    private fun initFatsecretData() {
        // Retrieve the fatsecret_id from the Intent
        val foodId = intent.getStringExtra("fatsecret_id")
        Log.d("ManualInputActivity", "Received food_id: $foodId")

        // Use the foodId to fetch more details or perform other actions
        if (foodId != null) {
            lifecycleScope.launch {
                val data = mapOf("foodId" to foodId)
                val result = functions.getHttpsCallable("fatsecretGet")
                    .call(data)
                    .await()
                populateFatsecretData(result.data)
            }
        }
    }

    private fun populateFatsecretData(foodData: Any?) {
        if (foodData == null) {
            Log.d("populateFatsecretData", "No data received")
            return
        }

        // Cast data to a Map if it's in that format
        val foodDataMap = foodData as? Map<*, *> ?: return

        // Extract the main "food" object
        val food = foodDataMap["food"] as? Map<*, *> ?: return

        // Extract general information about the food
        val foodName = food["food_name"] as? String ?: ""
        val brandName = food["brand_name"] as? String ?: ""
        val servings = food["servings"] as? Map<*, *>
        val servingList = servings?.get("serving") as? List<*>

        // Extract serving information (assuming only one serving in the list)
        val firstServing = servingList?.firstOrNull() as? Map<*, *>

        // Extract nutrient information from the first serving
        val servingDescription = firstServing?.get("serving_description") as? String ?: ""
        val calories = (firstServing?.get("calories") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val carbohydrate = (firstServing?.get("carbohydrate") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val protein = (firstServing?.get("protein") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val fat = (firstServing?.get("fat") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val saturatedFat = (firstServing?.get("saturated_fat") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val sodium = (firstServing?.get("sodium") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val fiber = (firstServing?.get("fiber") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val unsaturatedFat = (firstServing?.get("unsaturated_fat") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val transFat = (firstServing?.get("trans_fat") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val cholesterol = (firstServing?.get("cholesterol") as? String)?.toDoubleOrNull()?.toInt() ?: 0
        val sugar = (firstServing?.get("sugar") as? String)?.toDoubleOrNull()?.toInt() ?: 0

        // Find the EditText fields and populate them with data
        findViewById<EditText>(R.id.inputFoodName).setText(foodName)
        findViewById<EditText>(R.id.inputServingType).setText(servingDescription)

        // Populate numeric fields after converting them to integers
        findViewById<EditText>(R.id.inputCalories).setText(calories.toString())
        findViewById<EditText>(R.id.inputCarbohydrates).setText(carbohydrate.toString())
        findViewById<EditText>(R.id.inputProtein).setText(protein.toString())
        findViewById<EditText>(R.id.inputFats).setText(fat.toString())
        findViewById<EditText>(R.id.inputSaturatedFat).setText(saturatedFat.toString())
        findViewById<EditText>(R.id.inputSodium).setText(sodium.toString())
        findViewById<EditText>(R.id.inputFiber).setText(fiber.toString())
        findViewById<EditText>(R.id.inputUnsaturatedFat).setText(unsaturatedFat.toString())
        findViewById<EditText>(R.id.inputTransFat).setText(transFat.toString())
        findViewById<EditText>(R.id.inputCholesterol).setText(cholesterol.toString())
        findViewById<EditText>(R.id.inputSugar).setText(sugar.toString())
    }

    private fun initFoodData() {
        // Retrieve the food_id from the Intent
        val foodId = intent.getStringExtra("food_id")
        Log.d("ManualInputActivity", "Received food_id: $foodId")

        // Use the foodId to fetch more details or perform other actions
        if (foodId != null) {
            lifecycleScope.launch {
                val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                val authId = sharedPreferences.getString("authId", null)
                val data = mapOf(
                    "authId" to authId,
                    "foodId" to foodId
                )
                val result = functions.getHttpsCallable("getFood")
                    .call(data)
                    .await()
                populateFoodData(result.data)
            }
        }
    }

    private fun populateFoodData(foodData: Any?) {
        if (foodData == null) {
            Log.d("populateFoodData", "No data received")
            return
        }

        // Cast data to a Map if it's in that format
        val foodDataMap = foodData as? Map<*, *> ?: return

        // Extract general information about the food
        val foodName = foodDataMap["name"] as? String ?: ""
        val servingDescription = foodDataMap["servingType"] as? String ?: ""
        val calories = (foodDataMap["calories"] as? Number)?.toInt() ?: 0
        val fat = (foodDataMap["fat"] as? Number)?.toInt() ?: 0
        val saturatedFat = (foodDataMap["saturatedFat"] as? Number)?.toInt() ?: 0
        val unsaturatedFat = (foodDataMap["unsaturatedFat"] as? Number)?.toInt() ?: 0
        val transFat = (foodDataMap["transFat"] as? Number)?.toInt() ?: 0
        val sodium = (foodDataMap["sodium"] as? Number)?.toInt() ?: 0
        val carbohydrate = (foodDataMap["carbs"] as? Number)?.toInt() ?: 0
        val protein = (foodDataMap["protein"] as? Number)?.toInt() ?: 0
        val sugar = (foodDataMap["sugar"] as? Number)?.toInt() ?: 0
        val fiber = (foodDataMap["fiber"] as? Number)?.toInt() ?: 0

        // Find the EditText fields and populate them with data
        findViewById<EditText>(R.id.inputFoodName).setText(foodName)
        findViewById<EditText>(R.id.inputServingType).setText(servingDescription)

        // Populate numeric fields after converting them to integers
        findViewById<EditText>(R.id.inputCalories).setText(calories.toString())
        findViewById<EditText>(R.id.inputCarbohydrates).setText(carbohydrate.toString())
        findViewById<EditText>(R.id.inputProtein).setText(protein.toString())
        findViewById<EditText>(R.id.inputFats).setText(fat.toString())
        findViewById<EditText>(R.id.inputSaturatedFat).setText(saturatedFat.toString())
        findViewById<EditText>(R.id.inputUnsaturatedFat).setText(unsaturatedFat.toString())
        findViewById<EditText>(R.id.inputTransFat).setText(transFat.toString())
        findViewById<EditText>(R.id.inputSodium).setText(sodium.toString())
        findViewById<EditText>(R.id.inputSugar).setText(sugar.toString())
        findViewById<EditText>(R.id.inputFiber).setText(fiber.toString())

        // These fields are not available in this data format, so we'll set them to zero
        findViewById<EditText>(R.id.inputCholesterol).setText("0")
    }
}