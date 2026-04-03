package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlacesResponse(
    @Json(name = "results") val results: List<PlaceResult>,
    @Json(name = "status") val status: String,
    @Json(name = "next_page_token") val nextPageToken: String? = null
)
