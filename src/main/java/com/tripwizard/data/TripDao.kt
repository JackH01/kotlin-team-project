package com.tripwizard.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface TripDao {
//    @Query("SELECT trips.*, attractions.* from trips LEFT OUTER JOIN attractions ON trips.id = attractions.tripId ORDER BY id ASC, attractions.tripId ASC")
//    fun getAllTripsAndAttractions(): Flow<List<TripWithAttractions>>

    @Transaction
    @Query("SELECT * from trips ORDER BY id ASC")
    fun getAllTripsAndAttractions(): Flow<List<TripWithAttractionsAndLabels>>

    @Query("SELECT * from trips ORDER BY id ASC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * from trips WHERE id = :id")
    fun getTrip(id: Int): Flow<Trip>

    @Query("SELECT * from trips ORDER BY id DESC LIMIT 1")
    fun getNewestTrip(): Flow<Trip>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trip: Trip): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabels(labels: List<Label>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttractions(attractions: List<Attraction>)

    @Transaction
    suspend fun insertTripWithLabels(trip: Trip, labels: List<Label>) {
        val tripId = insert(trip)
        insertLabels(labels.map {
            it.copy(tripId = tripId.toInt())
        })
    }

    @Transaction
    suspend fun addNewTripWithLabelsAndAttractions(trip: Trip, labels: List<Label>, attractions: List<Attraction>) {
        val tripId = insert(trip)
        insertLabels(labels.map {
            it.copy(tripId = tripId.toInt())
        })
        insertAttractions(attractions.map {
            it.copy(tripId = tripId.toInt())
        })
    }

    @Update
    suspend fun update(trip: Trip)


    @Update
    suspend fun updateLabels(labels: List<Label>)

    @Transaction
    suspend fun updateTripWithLabels(
        tripWithAttractionsAndLabels: TripWithAttractionsAndLabels,
        newLabels: List<Label>
    ) {
        update(tripWithAttractionsAndLabels.trip)
        deleteLabels(tripWithAttractionsAndLabels.labels)
        insertLabels(newLabels.map {
            it.copy(tripId = tripWithAttractionsAndLabels.trip.id)
        })
    }

    @Delete
    suspend fun delete(trip: Trip)

    @Query("DELETE FROM labels WHERE tripId = :tripId")
    suspend fun deleteLabelsByTripId(tripId: Int)

    @Query("DELETE FROM attractions WHERE tripId = :tripId")
    suspend fun deleteAttractionsByTripId(tripId: Int)


    @Delete
    suspend fun deleteLabels(labels: List<Label>)

    @Delete
    suspend fun deleteAttractions(attractions: List<Attraction>)
}