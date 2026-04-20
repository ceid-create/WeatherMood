package com.charbeljoe.weathermood.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charbeljoe.weathermood.data.repository.WeatherRepository

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.getSharedPreferences(
                "weathermood_prefs", Context.MODE_PRIVATE
            )
            val lat = prefs.getFloat("last_lat", 0f).toDouble()
            val lon = prefs.getFloat("last_lon", 0f).toDouble()

            if (lat == 0.0 && lon == 0.0) return Result.success()

            val repository = WeatherRepository(applicationContext)
            val weather = repository.getWeather(lat, lon)
            val condition = weather.weather.firstOrNull()?.main ?: "Clear"
            val placeType = mapWeatherToPlaceType(condition)
            repository.getNearbyPlaces(lat, lon, placeType)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun mapWeatherToPlaceType(condition: String): String {
        return when (condition) {
            "Clear" -> "park"
            "Clouds" -> "cafe"
            "Rain", "Drizzle", "Thunderstorm" -> "restaurant"
            "Snow" -> "cafe"
            else -> "restaurant"
        }
    }
}
