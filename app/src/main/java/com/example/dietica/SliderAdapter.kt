package com.example.dietica

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class SliderAdapter(private val progressValues: List<Int>) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar1: ProgressBar = itemView.findViewById(R.id.progressBar1)
        val progressBar2: ProgressBar = itemView.findViewById(R.id.progressBar1)
        val progressBar3: ProgressBar = itemView.findViewById(R.id.progressBar1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.progressBar1.progress = progressValues[position]
        holder.progressBar2.progress = progressValues[position]
        holder.progressBar3.progress = progressValues[position]
    }

    override fun getItemCount(): Int {
        return progressValues.size
    }
}