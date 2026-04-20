package com.charbeljoe.weathermood.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.charbeljoe.weathermood.data.remote.models.PlaceResult
import com.charbeljoe.weathermood.databinding.ItemPlaceBinding
import com.charbeljoe.weathermood.util.loadPlaceImage

class PlaceListAdapter(
    private val onFavoriteClick: (PlaceResult) -> Unit
) : ListAdapter<PlaceResult, PlaceListAdapter.PlaceViewHolder>(DiffCallback()) {

    inner class PlaceViewHolder(val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = getItem(position)
        val b = holder.binding
        b.placeName.text = place.name
        b.placeVicinity.text = place.vicinity
        b.placeRating.text = place.rating?.let { "★ %.1f".format(it) } ?: ""
        b.placeImage.loadPlaceImage(place.photos?.firstOrNull()?.photoReference)
        
        if (place.isOpen != null) {
            b.placeStatus.visibility = View.VISIBLE
            if (place.isOpen == true) {
                b.placeStatus.text = "Open Now"
                b.placeStatus.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                b.placeStatus.text = "Closed"
                b.placeStatus.setTextColor(Color.parseColor("#F44336"))
            }
        } else {
            b.placeStatus.visibility = View.GONE
        }

        b.favoriteButton.setOnClickListener { onFavoriteClick(place) }
    }

    class DiffCallback : DiffUtil.ItemCallback<PlaceResult>() {
        override fun areItemsTheSame(oldItem: PlaceResult, newItem: PlaceResult) =
            oldItem.placeId == newItem.placeId

        override fun areContentsTheSame(oldItem: PlaceResult, newItem: PlaceResult) =
            oldItem == newItem
    }
}
