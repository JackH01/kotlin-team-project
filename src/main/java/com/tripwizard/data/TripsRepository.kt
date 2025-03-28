package com.tripwizard.data

import kotlinx.coroutines.flow.Flow

interface TripsRepository {
    fun getAllTripsAndAttractionsStream(): Flow<List<TripWithAttractionsAndLabels>>
    fun getAllTripsStream(): Flow<List<Trip>>

    fun getTripStream(id: Int): Flow<Trip?>

    fun getNewestTrip(): Flow<Trip?>

    suspend fun insertTrip(trip: Trip): Long

    suspend fun insertLabels(labels: List<Label>)
    suspend fun insertTripWithLabels(trip: Trip, labels: List<Label>)

    suspend fun deleteTrip(trip: Trip)

    suspend fun updateTrip(trip: Trip)

    suspend fun updateLabels(labels: List<Label>)

    suspend fun deleteLabelsByTripId(tripId: Int)

    suspend fun deleteAttractionsByTripId(tripId: Int)

    suspend fun updateTripWithLabels(
        tripWithAttractionsAndLabels: TripWithAttractionsAndLabels,
        newLabels: List<Label>
    )
    suspend fun addNewTripWithLabelsAndAttractions(
        trip: Trip, labels: List<Label>, attractions: List<Attraction>
    )
}