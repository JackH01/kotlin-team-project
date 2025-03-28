package com.tripwizard.data

import kotlinx.coroutines.flow.Flow

interface AttractionRepository {

    fun getAllAttractionsStream(trip: Trip): Flow<List<Attraction>>

    fun getAttractionStream(id: Int): Flow<Attraction?>

    suspend fun insertAttraction(attraction: Attraction)

    suspend fun deleteAttraction(attraction: Attraction)

    suspend fun updateAttraction(attraction: Attraction)

}