package com.tripwizard.ui.shared

import com.tripwizard.data.Notification

data class DarkModePreferredUiState(
    val darkModePreferred: Boolean? = null
)

data class UsernameUiState(
    val username: String = ""
)

data class LatestUserLocationUiState(
    val location: Coordinates? = null
)

data class Coordinates(val latitude: Double, val longitude: Double)

data class NotificationsUiState(
    val notificationsList: List<Notification> = listOf()
)