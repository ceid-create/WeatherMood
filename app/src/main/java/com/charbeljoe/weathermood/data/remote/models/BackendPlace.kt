package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackendPlace(
    @Json(name = "_id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "weatherCondition") val weatherCondition: String,
    @Json(name = "rating") val rating: Double? = null
)
