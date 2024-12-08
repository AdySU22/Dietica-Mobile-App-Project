package com.example.dietica

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dietica.services.LoadingUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil
import kotlin.random.Random


class HealthReportActivity : BaseActivity() {
    private lateinit var functions: FirebaseFunctions

    private lateinit var progressOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_health_report)

        functions = FirebaseFunctions.getInstance()

        // Decide whether to use the emulator
        if (false) {
            // Replace "10.0.2.2" with "localhost" if testing on a physical device
            functions.useEmulator("10.0.2.2", 5001)
            Log.d("FirebaseEmulator", "Using Firebase Emulator for Functions")
        }

        // Initialize loading
        progressOverlay = findViewById(R.id.progress_overlay)

        // Back Button Action
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Weekly Text Visibility
        val weeklyText: TextView = findViewById(R.id.weeklyText)
        weeklyText.visibility = View.VISIBLE

        // Setup the BarChart
        val barChart: BarChart = findViewById(R.id.barChart)
        setupBarChart(barChart)

        // Setup the LineChart
        val lineChart: LineChart = findViewById(R.id.lineChart)
        setupLineChart(lineChart)

        setupData(barChart, lineChart)

        // Initialize Download Report PDF button
        initPdfButton()
    }

    private fun setupBarChart(barChart: BarChart) {
        // Prepare data entries
        val entries = listOf(
            BarEntry(0f, 150f), // Sun
            BarEntry(1f, 100f), // Mon
            BarEntry(2f, 180f), // Tue
            BarEntry(3f, 220f), // Wed
            BarEntry(4f, 160f), // Thu
            BarEntry(5f, 120f), // Fri
            BarEntry(6f, 140f)  // Sat
        )

        // Create a BarDataSet and style it
        val barDataSet = BarDataSet(entries, "Weekly Average")
        barDataSet.valueTextSize = 12f

        // Set gradient color for each bar
        barDataSet.setGradientColor(
            ContextCompat.getColor(this, R.color.start_color_bar),
            ContextCompat.getColor(this, R.color.end_color_bar)
        )

        // Create BarData and set it to the chart
        val barData = BarData(barDataSet)
        barChart.data = barData

        // Customize the XAxis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = XAxisValueFormatter() // Custom formatter for weekday labels
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 12f

        // Customize YAxis
        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.granularity = 50f
        leftAxis.axisMinimum = 0f

        val rightAxis: YAxis = barChart.axisRight
        rightAxis.isEnabled = false

        // General chart styling
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.animateY(1000)
        barChart.invalidate() // Refresh the chart
    }

    private fun setupLineChart(lineChart: LineChart) {
        // Prepare data entries for LineChart
        val caloriesEntries = listOf(
            Entry(0f, 50000f),
            Entry(1f, 150000f),
            Entry(2f, 90000f),
            Entry(3f, 150000f),
            Entry(4f, 10000f),
            Entry(5f, 70000f),
            Entry(6f, 120000f)
        )

        val carbsEntries = listOf(
            Entry(0f, 20000f),
            Entry(1f, 40000f),
            Entry(2f, 60000f),
            Entry(3f, 80000f),
            Entry(4f, 20000f),
            Entry(5f, 10000f),
            Entry(6f, 100000f)
        )

        val proteinEntries = listOf(
            Entry(0f, 15000f),
            Entry(1f, 10000f),
            Entry(2f, 25000f),
            Entry(3f, 45000f),
            Entry(4f, 70000f),
            Entry(5f, 80000f),
            Entry(6f, 45000f)
        )

        val fatsEntries = listOf(
            Entry(0f, 10000f),
            Entry(1f, 30000f),
            Entry(2f, 60000f),
            Entry(3f, 120000f),
            Entry(4f, 40000f),
            Entry(5f, 40000f),
            Entry(6f, 80000f)
        )

        // Create LineDataSets for each dataset
        val caloriesDataSet = LineDataSet(caloriesEntries, "Calories (cal)").apply {
            color = ContextCompat.getColor(this@HealthReportActivity, R.color.purple_line_chart)
            lineWidth = 2f
            setDrawValues(false)
        }

        val carbsDataSet = LineDataSet(carbsEntries, "Net. Carbs (g)").apply {
            color = ContextCompat.getColor(this@HealthReportActivity, R.color.blue_line_chart)
            lineWidth = 2f
            setDrawValues(false)
        }

        val proteinDataSet = LineDataSet(proteinEntries, "Protein (g)").apply {
            color = ContextCompat.getColor(this@HealthReportActivity, R.color.orange_line_chart)
            lineWidth = 2f
            setDrawValues(false)
        }

        val fatsDataSet = LineDataSet(fatsEntries, "Fats (g)").apply {
            color = ContextCompat.getColor(this@HealthReportActivity, R.color.green_line_chart)
            lineWidth = 2f
            setDrawValues(false)
        }

        // Combine all datasets
        val lineData = LineData(caloriesDataSet, carbsDataSet, proteinDataSet, fatsDataSet)

        // Set the data to LineChart
        lineChart.data = lineData

        // Configure chart appearance
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            animateX(1500)

            // Customize the legend
            legend.apply {
                form = Legend.LegendForm.LINE
                textSize = 12f
                textColor = ContextCompat.getColor(this@HealthReportActivity, R.color.black)
            }
        }

        // Customize the XAxis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MonthValueFormatter() // Custom formatter for month labels
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 12f

        // Customize YAxis
        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f

        val rightAxis: YAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        // Refresh the chart with new data
        lineChart.invalidate()
    }

    // Custom XAxisValueFormatter to show days of the week
    private class XAxisValueFormatter : ValueFormatter() {
        private fun getDaysOfWeek(): Array<String> {
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("EEE", Locale.getDefault()) // Format to get short day name

            // List to store the days of the week
            val days = mutableListOf<String>()

            // Generate days starting from tomorrow
            for (i in 1..7) {
                calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
                days.add(sdf.format(calendar.time))   // Add the day to the list
            }
            return days.toTypedArray()
        }

        private val days = getDaysOfWeek()

        override fun getFormattedValue(value: Float): String {
            return days.getOrElse(value.toInt()) { "" }
        }
    }

    // Custom formatter for months in LineChart
    private class MonthValueFormatter : ValueFormatter() {
        private fun getDaysOfWeek(): Array<String> {
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("EEE", Locale.getDefault()) // Format to get short day name

            // List to store the days of the week
            val days = mutableListOf<String>()

            // Generate days starting from tomorrow
            for (i in 1..7) {
                calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
                days.add(sdf.format(calendar.time))   // Add the day to the list
            }
            return days.toTypedArray()
        }

        private val days = getDaysOfWeek()

        override fun getFormattedValue(value: Float): String {
            return days.getOrElse(value.toInt()) { "" }
        }
    }

    private fun setupData(barChart: BarChart, lineChart: LineChart) {
        val averageWaterIntake = findViewById<TextView>(R.id.averageWaterIntakeText2)
        val averageCalorieIntake = findViewById<TextView>(R.id.averageCalorieIntakeText2)

        lifecycleScope.launch {
            try {
                // Start loading overlay
                LoadingUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)

                // Get the default time zone and shared preferences
                val iataTimeZone = TimeZone.getDefault().id
                val sharedPreferences = getSharedPreferences("com.example.dietica", MODE_PRIVATE)
                val authId = sharedPreferences.getString("authId", null)

                // Prepare the data to be sent
                val data = mapOf(
                    "authId" to authId,
                    "iataTimeZone" to iataTimeZone
                )

                // Call the cloud function and await the result
                val result = functions.getHttpsCallable("getReport")
                    .call(data)
                    .await()

                // Parse the result using Gson
                val gson = Gson()
                val json = gson.toJson(result.data)
                val resultData = gson.fromJson(json, ResultData::class.java)

                // Update the UI with the fetched data
                averageWaterIntake.text = "${ceil(resultData.averageWater).toInt()} ml"
                averageCalorieIntake.text = "${ceil(resultData.averageCalories).toInt()} cals"

                // Setup charts with the fetched data
                setupBarChartData(barChart, resultData.dailyExerciseMinutes)
                setupLineChartData(lineChart, resultData.dailyIntakes)

                // Update Weight and Height
                findViewById<TextView>(R.id.weightText2).text = String.format("%.1f kg", resultData.weight)
                findViewById<TextView>(R.id.heightText2).text = String.format("%.1f cm", resultData.height)

                // Update BMI
                val bmiValue = (resultData.bmi as? Number)?.toDouble() ?: 0.0
                findViewById<TextView>(R.id.bmiNumberText).text = String.format("%.2f", bmiValue)

                // Update BMI Category
                val bmiCategoryTextView = findViewById<TextView>(R.id.bmiCategoryText)
                val bmiCategory = resultData.bmiCategory as? String ?: "Unknown"
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
                val bmiUpdatedAt = resultData.bmiUpdatedAt
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
            } catch (e: FirebaseFunctionsException) {
                when (e.code) {
                    FirebaseFunctionsException.Code.FAILED_PRECONDITION -> {
                        Log.e("getReportError", "Failed precondition: ${e.message}", e)
                        Toast.makeText(
                            this@HealthReportActivity,
                            "Failed to generate report. ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Log.e("getReportError", "Unexpected Firebase Functions error: ${e.message}", e)
                        Toast.makeText(
                            this@HealthReportActivity,
                            "Failed to generate report. Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // Handle generic exceptions
                Log.e("getReportError", "Error fetching report data: ${e.message}", e)
                Toast.makeText(
                    this@HealthReportActivity,
                    "Failed to generate report. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                // Hide loading overlay
                LoadingUtils.animateView(progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun setupBarChartData(barChart: BarChart, dailyExerciseMinutes: List<Double>) {
        val barData = barChart.data

        if (barData != null && barData.dataSetCount > 0) {
            // Get the existing BarDataSet
            val barDataSet = barData.getDataSetByIndex(0) as BarDataSet

            // Update the entries in the BarDataSet
            barDataSet.values = dailyExerciseMinutes.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value.toFloat())
            }

            // Notify that the data has changed
            barDataSet.notifyDataSetChanged()
            barData.notifyDataChanged()
            barChart.notifyDataSetChanged()

            // Refresh the chart
            barChart.invalidate()
        }
    }

    private fun setupLineChartData(lineChart: LineChart, dailyIntakes: DailyIntakes) {
        val lineData = lineChart.data

        if (lineData != null && lineData.dataSetCount > 0) {
            // Get the existing LineDataSet
            val caloriesDataSet = lineData.getDataSetByIndex(0) as LineDataSet
            val carbsDataSet = lineData.getDataSetByIndex(1) as LineDataSet
            val proteinDataSet = lineData.getDataSetByIndex(2) as LineDataSet
            val fatsDataSet = lineData.getDataSetByIndex(3) as LineDataSet

            // Update the entries in the LineDataSet
            caloriesDataSet.values = dailyIntakes.dailyCalories.mapIndexed { index, value ->
                Entry(index.toFloat(), value.toFloat())
            }
            carbsDataSet.values = dailyIntakes.dailyCarbs.mapIndexed { index, value ->
                Entry(index.toFloat(), value.toFloat())
            }
            proteinDataSet.values = dailyIntakes.dailyProtein.mapIndexed { index, value ->
                Entry(index.toFloat(), value.toFloat())
            }
            fatsDataSet.values = dailyIntakes.dailyFat.mapIndexed { index, value ->
                Entry(index.toFloat(), value.toFloat())
            }

            // Notify that the data has changed
            caloriesDataSet.notifyDataSetChanged()
            carbsDataSet.notifyDataSetChanged()
            proteinDataSet.notifyDataSetChanged()
            fatsDataSet.notifyDataSetChanged()
            lineData.notifyDataChanged()
            lineChart.notifyDataSetChanged()

            // Refresh the chart
            lineChart.invalidate()
        }
    }

    data class ResultData(
        val weight: Double,
        val height: Double,
        val bmi: Double,
        val bmiCategory: String,
        val bmiUpdatedAt: Any,
        val averageWater: Double,
        val averageCalories: Double,
        val dailyIntakes: DailyIntakes,
        val dailyExerciseMinutes: List<Double>
    )

    data class DailyIntakes(
        val dailyCalories: List<Double>,
        val dailyCarbs: List<Double>,
        val dailyFat: List<Double>,
        val dailyProtein: List<Double>
    )

    private fun initPdfButton() {
        // Button to trigger screenshot
        val screenshotButton: Button = findViewById(R.id.downloadReportButton)
        screenshotButton.setOnClickListener {
            // Capture the screenshot
            val scrollView: ScrollView = findViewById(R.id.scrollView) // Replace with your ScrollView ID
            val screenshot = getFullScrollViewScreenshot(scrollView)
            if (screenshot != null) {
                val fileName = generateFileName()
                savePdfToDownloads(screenshot, fileName)
                Toast.makeText(this, "PDF saved to Downloads!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Generate file name
    private fun generateFileName(): String {
        // Get the current date in DD-MM-YYYY format
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Generate a random 4-digit ID
        val randomId = Random.nextInt(1000, 9999)

        // Combine into the desired format
        return "Dietica Report $currentDate-$randomId.pdf"
    }

    // Method to capture the full ScrollView screenshot
    private fun getFullScrollViewScreenshot(scrollView: ScrollView): Bitmap {
        // Measure the full height of the ScrollView content
        val height = scrollView.getChildAt(0).height
        val width = scrollView.width

        // Create a Bitmap to hold the content
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Draw the content onto a Canvas backed by the Bitmap
        val canvas = Canvas(bitmap)
        scrollView.draw(canvas)

        return bitmap
    }

    private fun savePdfToDownloads(bitmap: Bitmap, fileName: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // API 29+ (Scoped Storage)
            savePdfUsingMediaStore(bitmap, fileName)
        } else {
            // API 28 and below
            savePdfToLegacyDownloads(bitmap, fileName)
        }
    }

    // Save PDF using MediaStore for API 29+
    @SuppressLint("NewApi")
    private fun savePdfUsingMediaStore(bitmap: Bitmap, fileName: String) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                contentResolver.openOutputStream(uri).use { outputStream ->
                    pdfDocument.writeTo(outputStream!!)
                }
                Toast.makeText(this, "PDF saved to Downloads folder", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            }

            pdfDocument.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Save PDF directly to Downloads directory for API 28 and below
    private fun savePdfToLegacyDownloads(bitmap: Bitmap, fileName: String) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            // Use the Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }

            Toast.makeText(this, "PDF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()

            pdfDocument.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
