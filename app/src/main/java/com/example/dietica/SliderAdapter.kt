package com.example.dietica

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(
    private val progressValues: List<Int>,
    private val layoutTypes: List<Int> // List to specify the layout type for each item
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    // View types for different layouts
    companion object {
        const val LAYOUT_TYPE_1 = 1
        const val LAYOUT_TYPE_2 = 2
        const val LAYOUT_TYPE_3 = 3
    }

    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar1: ProgressBar? = itemView.findViewById(R.id.progressBar1)
        val progressBar2: ProgressBar? = itemView.findViewById(R.id.progressBar2)
        val progressBar3: ProgressBar? = itemView.findViewById(R.id.progressBar3)
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
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        // Bind data to the ProgressBar if it's present in the layout
        holder.progressBar1?.progress = progressValues[position]
        holder.progressBar2?.progress = progressValues[position]
        holder.progressBar3?.progress = progressValues[position]
    }

    override fun getItemCount(): Int {
        return progressValues.size
    }
}
