package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    @Json(name = "list") val list: List<ForecastItem>,
    @Json(name = "city") val city: City
)

@JsonClass(generateAdapter = true)
data class ForecastItem(
    @Json(name = "dt") val dt: Long,
    @Json(name = "main") val main: MainData,
    @Json(name = "weather") val weather: List<WeatherCondition>,
    @Json(name = "dt_txt") val dtTxt: String
)

@JsonClass(generateAdapter = true)
data class City(
    @Json(name = "name") val name: String
)
