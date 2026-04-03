package com.charbeljoe.weathermood.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoritePlaceDao {

    @Query("SELECT * FROM favorite_places ORDER BY id DESC")
    fun getAll(): LiveData<List<FavoritePlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: FavoritePlace)

    @Delete
    suspend fun delete(place: FavoritePlace)
}
