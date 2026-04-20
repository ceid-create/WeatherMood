package com.charbeljoe.weathermood.data.remote

import com.charbeljoe.weathermood.data.remote.models.NearbySearchRequest
import com.charbeljoe.weathermood.data.remote.models.NewPlacesResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PlacesApiService {

    @POST("./places:searchNearby")
    @Headers("X-Goog-FieldMask: places.id,places.displayName,places.formattedAddress,places.rating,places.location,places.types,places.photos")
    suspend fun searchNearby(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Body request: NearbySearchRequest
    ): NewPlacesResponse
}
