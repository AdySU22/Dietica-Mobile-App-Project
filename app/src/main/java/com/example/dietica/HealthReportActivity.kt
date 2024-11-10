package com.example.dietica

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.formatter.ValueFormatter

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

        // Setup the LineChart
        val lineChart: LineChart = findViewById(R.id.lineChart)
        setupLineChart(lineChart)
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
            Entry(0f, 50000f), // Oct
            Entry(1f, 150000f), // Nov
            Entry(2f, 90000f), // Dec
            Entry(3f, 150000f) // Jan
        )

        val carbsEntries = listOf(
            Entry(0f, 20000f),
            Entry(1f, 40000f),
            Entry(2f, 60000f),
            Entry(3f, 80000f)
        )

        val proteinEntries = listOf(
            Entry(0f, 15000f),
            Entry(1f, 10000f),
            Entry(2f, 25000f),
            Entry(3f, 45000f)
        )

        val fatsEntries = listOf(
            Entry(0f, 10000f),
            Entry(1f, 30000f),
            Entry(2f, 60000f),
            Entry(3f, 120000f)
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
        private val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        override fun getFormattedValue(value: Float): String {
            return days.getOrElse(value.toInt()) { "" }
        }
    }

    // Custom formatter for months in LineChart
    private class MonthValueFormatter : ValueFormatter() {
        private val months = arrayOf("Oct", "Nov", "Dec", "Jan")

        override fun getFormattedValue(value: Float): String {
            return months.getOrElse(value.toInt()) { "" }
        }
    }
}
