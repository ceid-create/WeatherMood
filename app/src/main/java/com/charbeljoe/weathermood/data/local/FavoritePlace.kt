package com.charbeljoe.weathermood.data.local // tells Android that the file belongs in this folder

import androidx.room.Entity
import androidx.room.PrimaryKey

// Create a table in the phone’s local database called favorite_places
@Entity(tableName = "favorite_places")
// @Entity tells Room: "create a table called favorite_places for this class". Each field becomes a column.
// @PrimaryKey(autoGenerate = true) means Room automatically assigns an ID number.
data class FavoritePlace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val weatherCondition: String,
    val username: String = ""
)
//That table is stored in a hidden SQLite database file