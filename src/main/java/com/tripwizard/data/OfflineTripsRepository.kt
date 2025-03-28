package com.tripwizard.data

import kotlinx.coroutines.flow.Flow

class OfflineTripsRepository(private val tripDao: TripDao) : TripsRepository {
    override fun getAllTripsAndAttractionsStream(): Flow<List<TripWithAttractionsAndLabels>> =
        tripDao.getAllTripsAndAttractions()

    override fun getAllTripsStream(): Flow<List<Trip>> = tripDao.getAllTrips()

    override fun getTripStream(id: Int): Flow<Trip?> = tripDao.getTrip(id)
    override fun getNewestTrip(): Flow<Trip?> = tripDao.getNewestTrip()

    override suspend fun insertTrip(trip: Trip): Long = tripDao.insert(trip)

    override suspend fun insertLabels(labels: List<Label>) = tripDao.insertLabels(labels)
    override suspend fun insertTripWithLabels(trip: Trip, labels: List<Label>) =
        tripDao.insertTripWithLabels(trip, labels)

    override suspend fun addNewTripWithLabelsAndAttractions(
        trip: Trip,
        labels: List<Label>,
        attractions: List<Attraction>
    ) =
        tripDao.addNewTripWithLabelsAndAttractions(trip, labels, attractions)

    override suspend fun deleteTrip(trip: Trip) = tripDao.delete(trip)

    override suspend fun updateTrip(trip: Trip) = tripDao.update(trip)

    override suspend fun updateLabels(labels: List<Label>) = tripDao.updateLabels(labels)

    override suspend fun updateTripWithLabels(
        tripWithAttractionsAndLabels: TripWithAttractionsAndLabels,
        newLabels: List<Label>
    ) =
        tripDao.updateTripWithLabels(tripWithAttractionsAndLabels, newLabels)

    override suspend fun deleteLabelsByTripId(tripId: Int) =
        tripDao.deleteLabelsByTripId(tripId)

    override suspend fun deleteAttractionsByTripId(tripId: Int) =
        tripDao.deleteAttractionsByTripId(tripId)
}