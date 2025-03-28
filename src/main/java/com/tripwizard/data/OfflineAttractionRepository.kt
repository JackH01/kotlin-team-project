package com.tripwizard.data;

import kotlinx.coroutines.flow.Flow

class OfflineAttractionRepository(private val attractionDao: AttractionDao) : AttractionRepository {
        override fun getAllAttractionsStream(trip: Trip): Flow<List<Attraction>> = attractionDao.getAllAttractionsForTrip(trip.id)

        override fun getAttractionStream(id: Int): Flow<Attraction?> = attractionDao.getAttraction(id)

        override suspend fun insertAttraction(attraction: Attraction) = attractionDao.insert(attraction)

        override suspend fun deleteAttraction(attraction: Attraction) = attractionDao.delete(attraction)

        override suspend fun updateAttraction(attraction: Attraction) = attractionDao.update(attraction)
}
