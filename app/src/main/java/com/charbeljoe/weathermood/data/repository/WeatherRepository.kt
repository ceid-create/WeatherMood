package com.charbeljoe.weathermood.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.charbeljoe.weathermood.BuildConfig
import com.charbeljoe.weathermood.data.local.AppDatabase
import com.charbeljoe.weathermood.data.local.FavoritePlace
import com.charbeljoe.weathermood.data.remote.RetrofitClient
import com.charbeljoe.weathermood.data.remote.models.BackendPlace
import com.charbeljoe.weathermood.data.remote.models.Circle
import com.charbeljoe.weathermood.data.remote.models.FavoriteRequest
import com.charbeljoe.weathermood.data.remote.models.ForecastResponse
import com.charbeljoe.weathermood.data.remote.models.Geometry
import com.charbeljoe.weathermood.data.remote.models.GeoLocation
import com.charbeljoe.weathermood.data.remote.models.LatLngBody
import com.charbeljoe.weathermood.data.remote.models.LocationRestriction
import com.charbeljoe.weathermood.data.remote.models.NearbySearchRequest
import com.charbeljoe.weathermood.data.remote.models.NewPlaceResult
import com.charbeljoe.weathermood.data.remote.models.PlacePhoto
import com.charbeljoe.weathermood.data.remote.models.PlaceResult
import com.charbeljoe.weathermood.data.remote.models.WeatherResponse

class WeatherRepository(context: Context) {

    private val weatherApi = RetrofitClient.weatherApi
    private val placesApi = RetrofitClient.placesApi
    private val backendApi = RetrofitClient.backendApi
    private val dao = AppDatabase.getInstance(context).favoritePlaceDao()

    fun getFavorites(username: String): LiveData<List<FavoritePlace>> = dao.getAll(username)

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return weatherApi.getCurrentWeather(lat, lon, BuildConfig.OPENWEATHER_API_KEY)
    }

    suspend fun getForecast(lat: Double, lon: Double): ForecastResponse {
        return weatherApi.getForecast(lat, lon, BuildConfig.OPENWEATHER_API_KEY)
    }

    suspend fun getNearbyPlaces(lat: Double, lng: Double, type: String, radius: Int = 1500): List<PlaceResult> {
        val request = NearbySearchRequest(
            includedTypes = listOf(type),
            maxResultCount = 20,
            locationRestriction = LocationRestriction(
                circle = Circle(
                    center = LatLngBody(lat, lng),
                    radius = radius.toDouble()
                )
            )
        )
        val response = placesApi.searchNearby(BuildConfig.GOOGLE_PLACES_API_KEY, request)
        return response.places?.map { it.toPlaceResult() } ?: emptyList()
    }

    private fun NewPlaceResult.toPlaceResult() = PlaceResult(
        placeId = id,
        name = displayName.text,
        vicinity = formattedAddress ?: "",
        rating = rating,
        geometry = Geometry(GeoLocation(location.latitude, location.longitude)),
        types = types ?: emptyList(),
        photos = photos?.map { photo ->
            PlacePhoto(
                photoReference = "https://places.googleapis.com/v1/${photo.name}/media?maxWidthPx=400&key=${BuildConfig.GOOGLE_PLACES_API_KEY}",
                height = photo.heightPx ?: 0,
                width = photo.widthPx ?: 0
            )
        },
        isOpen = currentOpeningHours?.openNow,
        openingHours = currentOpeningHours?.weekdayDescriptions
    )

    suspend fun getBackendPlaces(weatherCondition: String): List<BackendPlace> {
        return backendApi.getPlacesByWeather(weatherCondition)
    }

    suspend fun addFavoriteToBackend(favorite: FavoriteRequest): Boolean {
        return backendApi.addFavorite(favorite).isSuccessful
    }

    fun findFavorite(name: String, lat: Double, lng: Double, username: String) = dao.findByDetails(name, lat, lng, username)

    suspend fun saveFavorite(place: FavoritePlace) = dao.insert(place)

    suspend fun deleteFavorite(place: FavoritePlace) = dao.delete(place)
}
