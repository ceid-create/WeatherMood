package com.charbeljoe.weathermood.ui.favorites

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.charbeljoe.weathermood.data.local.FavoritePlace
import com.charbeljoe.weathermood.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val currentUsername get() = prefs.getString("username", "") ?: ""

    val favorites: LiveData<List<FavoritePlace>> = repository.getFavorites(currentUsername)

    val isEmpty: LiveData<Boolean> = favorites.map { it.isEmpty() }

    fun findFavorite(name: String, lat: Double, lng: Double) =
        repository.findFavorite(name, lat, lng, currentUsername)

    fun saveFavorite(place: FavoritePlace) {
        viewModelScope.launch { repository.saveFavorite(place) }
    }

    fun deleteFavorite(place: FavoritePlace) {
        viewModelScope.launch { repository.deleteFavorite(place) }
    }

    fun getUsername() = currentUsername
}
