package com.tripwizard.data

import androidx.room.Embedded
import androidx.room.Relation

data class TripWithAttractionsAndLabels(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val attractions: List<Attraction>,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val labels: List<Label>
)
