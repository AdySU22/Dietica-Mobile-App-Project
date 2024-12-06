package com.example.dietica

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(
    private val progressValues: List<List<Int>>, // List of progress values for each item
    private val layoutTypes: List<Int> // List to specify the layout type for each item
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    // View types for different layouts
    companion object {
        const val LAYOUT_TYPE_1 = 1
        const val LAYOUT_TYPE_2 = 2
        const val LAYOUT_TYPE_3 = 3
    }

    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var progressBars: List<ProgressBar> = emptyList()

        fun bindViewHolder(viewType: Int) {
            // Initialize progress bars based on layout type
            progressBars = when (viewType) {
                LAYOUT_TYPE_1 -> listOf(
                    itemView.findViewById(R.id.progressBarCarbs),
                    itemView.findViewById(R.id.progressBarFat),
                    itemView.findViewById(R.id.progressBarProtein)
                )
                LAYOUT_TYPE_2 -> listOf(
                    itemView.findViewById(R.id.progressBarSugar),
                    itemView.findViewById(R.id.progressBarSodium),
                    itemView.findViewById(R.id.progressBarCholesterol)
                )
                LAYOUT_TYPE_3 -> listOf(
                    itemView.findViewById(R.id.progressBarFiber),
                )
                else -> throw IllegalArgumentException("Invalid layout type")
            }
        }

        fun setProgressForBars(values: List<Int>) {
            // Dynamically update progress for each progress bar
            for (i in progressBars.indices) {
                progressBars[i].progress = values.getOrElse(i) { 0 }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // Return the layout type based on the position or the provided layoutTypes list
        return layoutTypes[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val layoutRes = when (viewType) {
            LAYOUT_TYPE_1 -> R.layout.item_slider_1
            LAYOUT_TYPE_2 -> R.layout.item_slider_2
            LAYOUT_TYPE_3 -> R.layout.item_slider_3
            else -> throw IllegalArgumentException("Invalid layout type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return SliderViewHolder(view).apply { bindViewHolder(viewType) }
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        // Bind progress values to the progress bars
        holder.setProgressForBars(progressValues[position])
    }

    override fun getItemCount(): Int {
        return progressValues.size
    }
}
