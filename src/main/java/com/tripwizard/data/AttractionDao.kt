package com.tripwizard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttractionDao {
    @Insert
    suspend fun insert(attraction: Attraction)

    @Update
    suspend fun update(attraction: Attraction)

    @Delete
    suspend fun delete(attraction: Attraction)

    @Query("SELECT * from " + Attraction.TABLE_NAME + " WHERE id = :id")
    fun getAttraction(id: Int): Flow<Attraction>

    @Query("SELECT * from " + Attraction.TABLE_NAME + " WHERE tripId = :tripId")
    fun getAllAttractionsForTrip(tripId: Int): Flow<List<Attraction>>
}