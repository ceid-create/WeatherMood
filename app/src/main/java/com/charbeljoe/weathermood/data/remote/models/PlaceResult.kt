package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaceResult(
    @Json(name = "place_id") val placeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "vicinity") val vicinity: String,
    @Json(name = "rating") val rating: Double? = null,
    @Json(name = "geometry") val geometry: Geometry,
    @Json(name = "types") val types: List<String>,
    @Json(name = "photos") val photos: List<PlacePhoto>? = null
)

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "location") val location: GeoLocation
)

@JsonClass(generateAdapter = true)
data class GeoLocation(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)

@JsonClass(generateAdapter = true)
data class PlacePhoto(
    @Json(name = "photo_reference") val photoReference: String,
    @Json(name = "height") val height: Int,
    @Json(name = "width") val width: Int
)
