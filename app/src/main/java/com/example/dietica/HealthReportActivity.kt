package com.example.dietica

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.*

class HealthReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_health_report)

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

    // Custom XAxisValueFormatter to show days of the week
    private class XAxisValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        private val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        override fun getFormattedValue(value: Float): String {
            return days.getOrElse(value.toInt()) { "" }
        }
    }
}
