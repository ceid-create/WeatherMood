package com.charbeljoe.weathermood.data.remote

import com.charbeljoe.weathermood.data.remote.models.BackendPlace
import com.charbeljoe.weathermood.data.remote.models.FavoriteRequest
import com.charbeljoe.weathermood.data.remote.models.ResetCodeRequest
import com.charbeljoe.weathermood.data.remote.models.VerifyCodeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BackendApiService {
    @GET("places")
    suspend fun getPlacesByWeather(
        @Query("weather") weather: String
    ): List<BackendPlace>

    @POST("favorites")
    suspend fun addFavorite(
        @Body favorite: FavoriteRequest
    ): Response<Unit>

    @POST("send-reset-code")
    suspend fun sendResetCode(
        @Body body: ResetCodeRequest
    ): Response<Unit>

    @POST("verify-reset-code")
    suspend fun verifyResetCode(
        @Body body: VerifyCodeRequest
    ): Response<Unit>
}
