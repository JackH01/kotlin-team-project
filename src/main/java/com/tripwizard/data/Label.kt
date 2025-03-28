package com.tripwizard.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

fun getLabelOptionsFromString(text: String): LabelOptions {
    return when (text) {
        "Active" -> LabelOptions.ACTIVE
        "Kid-Friendly" -> LabelOptions.KIDS
        "Beach" -> LabelOptions.BEACH
        "City Break" -> LabelOptions.CITY
        "Cultural" -> LabelOptions.CULTURE
        else -> throw IllegalArgumentException("Invalid label option: $text")
    }
}

enum class LabelOptions(val text: String) {
    ACTIVE("Active"),
    KIDS("Kid-Friendly"),
    BEACH("Beach"),
    CITY("City Break"),
    CULTURE("Cultural")
}

@Entity(
    tableName = "labels", foreignKeys = [ForeignKey(
        entity = Trip::class,
        parentColumns = ["id"],
        childColumns = ["tripId"]
    )]
)
data class Label(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: LabelOptions,
    val tripId: Int = 0
)
