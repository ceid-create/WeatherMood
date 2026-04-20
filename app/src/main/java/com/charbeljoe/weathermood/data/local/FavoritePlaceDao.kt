package com.charbeljoe.weathermood.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoritePlaceDao {

    @Query("SELECT * FROM favorite_places WHERE username = :username ORDER BY id DESC")
    fun getAll(username: String): LiveData<List<FavoritePlace>>

    @Query("SELECT * FROM favorite_places WHERE name = :name AND lat = :lat AND lng = :lng AND username = :username LIMIT 1")
    fun findByDetails(name: String, lat: Double, lng: Double, username: String): LiveData<FavoritePlace?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: FavoritePlace)

    @Delete
    suspend fun delete(place: FavoritePlace)
}
