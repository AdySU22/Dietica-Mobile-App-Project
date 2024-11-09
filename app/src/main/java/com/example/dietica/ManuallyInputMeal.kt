package com.example.dietica

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

        functions = FirebaseFunctions.getInstance()

        val btnBack: Button = findViewById(R.id.btnBack)
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
        val btnSave: Button = findViewById(R.id.btnSave)

        btnBack.setOnClickListener {
            finish()
        }

        // Cancel Button Click Listener
        btnCancel.setOnClickListener {
            clearFields(
                inputFoodName, inputServingType, inputCalories, inputFats,
                inputSaturatedFat, inputUnsaturatedFat, inputTransFat,
                inputCholesterol, inputSodium, inputCarbohydrates,
                inputProtein, inputSugar, inputFiber
            )
        }

        btnSave.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = createFood() // Assuming createFood() is now a suspend function
                    Log.d("FoodCreationResult", "Result: ${result.data}")
                    finish()
                } catch (e: Exception) {
                    // Handle exceptions (e.g., display an error message)
                    Log.e("FoodCreationError", "Error creating food: ${e.message}")
                    // Consider showing a Toast or Snackbar to the user
                }
            }
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

        val data = mapOf(
            "authId" to "authId-test-0", // TODO Replace with actual authId
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

}