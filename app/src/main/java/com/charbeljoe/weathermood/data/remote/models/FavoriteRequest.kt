package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FavoriteRequest(
    @Json(name = "placeId") val placeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)
