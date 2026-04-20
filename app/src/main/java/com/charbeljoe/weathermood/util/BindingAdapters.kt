package com.charbeljoe.weathermood.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.charbeljoe.weathermood.R

@BindingAdapter("placeImageUrl")
fun ImageView.loadPlaceImage(url: String?) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.ic_launcher_background)
        .error(R.drawable.ic_launcher_background)
        .into(this)
}
