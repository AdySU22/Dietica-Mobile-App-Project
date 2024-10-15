package com.example.dietica

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ManuallyInputMeal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manually_input_meal)

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
            finish()
        }

    }

    private fun clearFields(vararg fields: EditText) {
        for (field in fields) {
            field.text.clear()
        }
    }

}