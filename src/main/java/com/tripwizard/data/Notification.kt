package com.tripwizard.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class NotificationType {
    TIME,
    LOCATION
}

@Entity(
    tableName = "notifications",
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tripId: Int = 0,
    val type: NotificationType? = null,
    val shownToUser: Boolean = false,
    val unread: Boolean = true,
)
