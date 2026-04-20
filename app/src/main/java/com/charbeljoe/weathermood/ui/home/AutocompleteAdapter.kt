package com.charbeljoe.weathermood.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charbeljoe.weathermood.databinding.ItemAutocompleteBinding
import com.google.android.libraries.places.api.model.AutocompletePrediction

class AutocompleteAdapter(
    private val onItemClick: (AutocompletePrediction) -> Unit
) : RecyclerView.Adapter<AutocompleteAdapter.ViewHolder>() {

    private val predictions = mutableListOf<AutocompletePrediction>()

    fun submitPredictions(newPredictions: List<AutocompletePrediction>) {
        predictions.clear()
        predictions.addAll(newPredictions)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAutocompleteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAutocompleteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prediction = predictions[position]
        holder.binding.primaryText.text = prediction.getPrimaryText(null)
        holder.binding.secondaryText.text = prediction.getSecondaryText(null)
        holder.itemView.setOnClickListener { onItemClick(prediction) }
    }

    override fun getItemCount() = predictions.size
}
