package com.charbeljoe.weathermood.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_places")
data class FavoritePlace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val weatherCondition: String
)
