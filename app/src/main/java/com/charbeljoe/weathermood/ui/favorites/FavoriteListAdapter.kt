package com.charbeljoe.weathermood.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.charbeljoe.weathermood.data.local.FavoritePlace
import com.charbeljoe.weathermood.databinding.ItemFavoriteBinding

class FavoriteListAdapter(
    private val onDeleteClick: (FavoritePlace) -> Unit
) : ListAdapter<FavoritePlace, FavoriteListAdapter.FavoriteViewHolder>(DiffCallback()) {

    inner class FavoriteViewHolder(val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = getItem(position)
        holder.binding.favorite = favorite
        holder.binding.deleteButton.setOnClickListener { onDeleteClick(favorite) }
        holder.binding.executePendingBindings()
    }

    class DiffCallback : DiffUtil.ItemCallback<FavoritePlace>() {
        override fun areItemsTheSame(oldItem: FavoritePlace, newItem: FavoritePlace) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FavoritePlace, newItem: FavoritePlace) =
            oldItem == newItem
    }
}
