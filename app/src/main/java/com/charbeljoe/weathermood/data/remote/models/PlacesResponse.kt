package com.charbeljoe.weathermood.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewPlacesResponse(
    @Json(name = "places") val places: List<NewPlaceResult>? = null
)

@JsonClass(generateAdapter = true)
data class NewPlaceResult(
    @Json(name = "id") val id: String,
    @Json(name = "displayName") val displayName: DisplayName,
    @Json(name = "formattedAddress") val formattedAddress: String? = null,
    @Json(name = "rating") val rating: Double? = null,
    @Json(name = "location") val location: NewGeoLocation,
    @Json(name = "types") val types: List<String>? = null,
    @Json(name = "photos") val photos: List<NewPlacePhoto>? = null,
    @Json(name = "currentOpeningHours") val currentOpeningHours: OpeningHours? = null
)

@JsonClass(generateAdapter = true)
data class OpeningHours(
    @Json(name = "openNow") val openNow: Boolean? = null,
    @Json(name = "weekdayDescriptions") val weekdayDescriptions: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class DisplayName(
    @Json(name = "text") val text: String,
    @Json(name = "languageCode") val languageCode: String? = null
)

@JsonClass(generateAdapter = true)
data class NewGeoLocation(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

@JsonClass(generateAdapter = true)
data class NewPlacePhoto(
    @Json(name = "name") val name: String,
    @Json(name = "widthPx") val widthPx: Int? = null,
    @Json(name = "heightPx") val heightPx: Int? = null
)

@JsonClass(generateAdapter = true)
data class NearbySearchRequest(
    @Json(name = "includedTypes") val includedTypes: List<String>,
    @Json(name = "maxResultCount") val maxResultCount: Int = 20,
    @Json(name = "locationRestriction") val locationRestriction: LocationRestriction
)

@JsonClass(generateAdapter = true)
data class LocationRestriction(
    @Json(name = "circle") val circle: Circle
)

@JsonClass(generateAdapter = true)
data class Circle(
    @Json(name = "center") val center: LatLngBody,
    @Json(name = "radius") val radius: Double
)

@JsonClass(generateAdapter = true)
data class LatLngBody(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)
