package com.charbeljoe.weathermood.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.charbeljoe.weathermood.BuildConfig
import com.charbeljoe.weathermood.data.local.AppDatabase
import com.charbeljoe.weathermood.data.local.FavoritePlace
import com.charbeljoe.weathermood.data.remote.RetrofitClient
import com.charbeljoe.weathermood.data.remote.models.BackendPlace
import com.charbeljoe.weathermood.data.remote.models.FavoriteRequest
import com.charbeljoe.weathermood.data.remote.models.PlacesResponse
import com.charbeljoe.weathermood.data.remote.models.WeatherResponse

class WeatherRepository(context: Context) {

    private val weatherApi = RetrofitClient.weatherApi
    private val placesApi = RetrofitClient.placesApi
    private val backendApi = RetrofitClient.backendApi
    private val dao = AppDatabase.getInstance(context).favoritePlaceDao()

    val favorites: LiveData<List<FavoritePlace>> = dao.getAll()

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return weatherApi.getCurrentWeather(lat, lon, BuildConfig.OPENWEATHER_API_KEY)
    }

    suspend fun getNearbyPlaces(lat: Double, lng: Double, type: String, radius: Int = 1500): PlacesResponse {
        return placesApi.getNearbyPlaces("$lat,$lng", radius, type, BuildConfig.GOOGLE_PLACES_API_KEY)
    }

    suspend fun getBackendPlaces(weatherCondition: String): List<BackendPlace> {
        return backendApi.getPlacesByWeather(weatherCondition)
    }

    suspend fun addFavoriteToBackend(favorite: FavoriteRequest): Boolean {
        return backendApi.addFavorite(favorite).isSuccessful
    }

    suspend fun saveFavorite(place: FavoritePlace) = dao.insert(place)

    suspend fun deleteFavorite(place: FavoritePlace) = dao.delete(place)
}
