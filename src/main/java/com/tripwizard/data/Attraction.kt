package com.tripwizard.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate


enum class Priority(val text: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

@Entity(Attraction.TABLE_NAME)
data class Attraction(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val name: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val tripId: Int=0,
    val done: Boolean=false,
    val priority: Priority
) {
    companion object {
        const val TABLE_NAME = "attractions"
    }
}

