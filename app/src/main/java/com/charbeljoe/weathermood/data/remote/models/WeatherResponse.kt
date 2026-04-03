package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "weather") val weather: List<WeatherCondition>,
    @Json(name = "main") val main: MainData,
    @Json(name = "wind") val wind: WindData,
    @Json(name = "name") val cityName: String
)

@JsonClass(generateAdapter = true)
data class WindData(
    @Json(name = "speed") val speed: Double
)
