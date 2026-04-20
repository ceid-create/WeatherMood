package com.charbeljoe.weathermood.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.charbeljoe.weathermood.data.remote.models.ForecastItem
import com.charbeljoe.weathermood.data.remote.models.PlaceResult
import com.charbeljoe.weathermood.data.remote.models.WeatherResponse
import com.charbeljoe.weathermood.data.repository.WeatherRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    private val _places = MutableLiveData<List<PlaceResult>>()
    val places: LiveData<List<PlaceResult>> = _places

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _weatherDescription = MutableLiveData<String>()
    val weatherDescription: LiveData<String> = _weatherDescription

    private val _forecast = MutableLiveData<List<ForecastItem>>()
    val forecast: LiveData<List<ForecastItem>> = _forecast

    private val _availableDates = MutableLiveData<List<String>>()
    val availableDates: LiveData<List<String>> = _availableDates

    private val _availableHours = MutableLiveData<List<String>>()
    val availableHours: LiveData<List<String>> = _availableHours

    private val _displayTemp = MutableLiveData<String>()
    val displayTemp: LiveData<String> = _displayTemp

    private val _displayCondition = MutableLiveData<String>()
    val displayCondition: LiveData<String> = _displayCondition

    private val _displayIcon = MutableLiveData<String>()
    val displayIcon: LiveData<String> = _displayIcon

    private val _suggestedCities = MutableLiveData<List<String>>()
    val suggestedCities: LiveData<List<String>> = _suggestedCities

    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> = _locationName

    var currentLat: Double = 0.0
    var currentLon: Double = 0.0
    
    var selectedDate: String? = null
    var selectedHour: String? = null
    private var currentPlaceType: String = "restaurant"

    var isDataLoaded: Boolean = false

    fun loadData(lat: Double, lon: Double) {
        if (isDataLoaded && Math.abs(lat - currentLat) < 0.001 && Math.abs(lon - currentLon) < 0.001) return
        
        currentLat = lat
        currentLon = lon
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Fetch basic weather to get location name
                val weatherResponse = repository.getWeather(lat, lon)
                _locationName.value = weatherResponse.cityName

                // Fetch 5-day forecast
                val forecastResponse = repository.getForecast(lat, lon)
                val forecastList = forecastResponse.list
                _forecast.value = forecastList

                val dates = forecastList.map { it.dtTxt.split(" ")[0] }.distinct()
                _availableDates.value = dates

                // Default selections if null
                val now = Calendar.getInstance()
                if (selectedDate == null) {
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
                    selectedDate = if (dates.contains(todayStr)) todayStr else dates.firstOrNull()
                }
                
                if (selectedHour == null) {
                    val currentHour = now.get(Calendar.HOUR_OF_DAY)
                    val currentMin = now.get(Calendar.MINUTE)
                    selectedHour = if (currentMin < 30) {
                        "${currentHour.toString().padStart(2, '0')}:30"
                    } else {
                        "${((currentHour + 1) % 24).toString().padStart(2, '0')}:00"
                    }
                }

                // Initialize state
                isDataLoaded = true
                onDateSelected(selectedDate!!)
                applyWeatherAtTime(selectedDate!!, selectedHour!!)
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyWeatherAtTime(date: String, hour: String) {
        val forecastList = _forecast.value ?: return
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val targetTime = try { sdf.parse("$date $hour")?.time ?: 0L } catch(e: Exception) { 0L }

        // Find the absolute closest item in the 3-hour interval forecast
        val closestItem = forecastList.minByOrNull { item ->
            val itemTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.dtTxt)?.time ?: 0L
            Math.abs(itemTime - targetTime)
        }

        closestItem?.let {
            _displayTemp.value = "${it.main.temp.toInt()}°C"
            val condition = it.weather.firstOrNull()?.main ?: "Clear"
            _displayCondition.value = condition
            _displayIcon.value = it.weather.firstOrNull()?.icon ?: ""
            _weatherDescription.value = mapWeatherToDescription(condition)
            currentPlaceType = mapWeatherToPlaceType(condition)
            
            // Re-fetch places based on this weather condition
            viewModelScope.launch {
                fetchPlaces(currentLat, currentLon, currentPlaceType)
            }
        }
    }

    private suspend fun fetchPlaces(lat: Double, lon: Double, type: String) {
        _suggestedCities.value = emptyList() // Ensure Layer 1 dialog is removed for good

        // 1. Local Search (1.5km)
        val nearby = repository.getNearbyPlaces(lat, lon, type)
        if (nearby.isNotEmpty()) {
            _places.value = nearby
            _error.value = null
            return
        }

        // 2. Wide Search (50km) for the same MOOD type
        val wideMood = repository.getNearbyPlaces(lat, lon, type, radius = 50000)
        if (wideMood.isNotEmpty()) {
            _places.value = wideMood
            _weatherDescription.value = "We couldn't find matches for this mood nearby, so here are some recommendations slightly further away."
            _error.value = null
            return
        }

        // 3. Wide Search (50km) for ANY recommendations (Restaurants)
        val wideGeneral = repository.getNearbyPlaces(lat, lon, "restaurant", radius = 50000)
        if (wideGeneral.isNotEmpty()) {
            _places.value = wideGeneral
            _weatherDescription.value = "We couldn't find matches for this mood even nearby, so here are some general recommendations slightly further away."
            _error.value = null
        } else {
            _places.value = emptyList()
            _error.value = "Even within a 50 km radius, nothing was found matching this weather mood."
        }
    }

    fun onDateSelected(date: String) {
        val isNewDate = selectedDate != date
        selectedDate = date
        // Refresh weather/temp immediately when date changes
        if (isNewDate && isDataLoaded) {
            applyWeatherAtTime(date, selectedHour ?: "12:00")
        }
        
        // Populate hours list for the spinner
        val slots = mutableListOf<String>()
        for (h in 0..23) {
            val hStr = h.toString().padStart(2, '0')
            slots.add("$hStr:00")
            slots.add("$hStr:30")
        }
        _availableHours.value = slots
    }

    fun onHourSelected(hour: String) {
        val isNewHour = selectedHour != hour
        selectedHour = hour
        if (isNewHour && isDataLoaded) {
            selectedDate?.let { applyWeatherAtTime(it, hour) }
        }
    }

    private fun mapWeatherToPlaceType(condition: String): String {
        return when (condition) {
            "Clear" -> "park"
            "Clouds" -> "museum"
            "Rain", "Drizzle" -> "movie_theater"
            "Thunderstorm" -> "spa"
            "Snow" -> "bar"
            else -> "restaurant"
        }
    }

    private fun mapWeatherToDescription(condition: String): String {
        return when (condition) {
            "Clear" -> "The sun is practically begging you to go outside! It's the perfect day to be a tourist in your own city. How about soaking up the vitamin D in a park?"
            "Clouds" -> "Soft clouds and gentle light—ideal for a bit of culture. It's a great time to wander through a quiet museum gallery or lose yourself in the aisles of a bookstore."
            "Rain", "Drizzle" -> "Don't let the drizzle dampen your spirits! It's a sign to head indoors. Catch a movie, treat yourself at the mall, or watch the fish swim by at the aquarium."
            "Thunderstorm" -> "Nature is putting on a show! Stay safe and warm inside. This is the perfect excuse for some self-care at a spa, or watching the storm with a hot coffee."
            "Snow" -> "Winter wonderland mode: ON! Warm your soul with a hearty meal, find a cozy bar to hide from the cold, or hit the gym to keep the blood pumping."
            else -> "Here are some nearby spots that match today's weather."
        }
    }
}
