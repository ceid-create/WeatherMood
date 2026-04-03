package com.charbeljoe.weathermood.data.remote

import com.charbeljoe.weathermood.data.remote.models.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}
