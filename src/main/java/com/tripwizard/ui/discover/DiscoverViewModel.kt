package com.tripwizard.ui.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwizard.data.Notification
import com.tripwizard.data.NotificationRepository
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.data.TripsRepository
import com.tripwizard.data.UserSettingsRepository
import com.tripwizard.ui.STATEFLOW_TIMEOUT_MILLIS
import com.tripwizard.ui.shared.DarkModePreferredUiState
import com.tripwizard.ui.shared.NotificationsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val tripsRepository: TripsRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val discoverUiState: StateFlow<DiscoverUiState> =
        tripsRepository.getAllTripsAndAttractionsStream().map {
            DiscoverUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DiscoverUiState()
        )

    val notificationsUiState: StateFlow<NotificationsUiState> =
        notificationRepository.getAllNotificationsStream().map {
            NotificationsUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = NotificationsUiState()
        )

    val darkModePreferredUiState: StateFlow<DarkModePreferredUiState> =
        userSettingsRepository.darkModePreferred.map { darkModePreferred ->
            DarkModePreferredUiState(darkModePreferred)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DarkModePreferredUiState()
        )

    fun updateNotification(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.updateNotification(notification)
        }
    }

    fun insertNotifications(notifications: List<Notification>) {
        viewModelScope.launch {
            notificationRepository.insertNotificationList(notifications)
        }
    }

    fun addNewTripWithLabelsAndAttractions(tripWithLabelsAndAttractions: TripWithAttractionsAndLabels) {
        viewModelScope.launch {
            tripsRepository.addNewTripWithLabelsAndAttractions(
                trip = tripWithLabelsAndAttractions.trip,
                attractions = tripWithLabelsAndAttractions.attractions,
                labels = tripWithLabelsAndAttractions.labels
            )
        }
    }
}

data class DiscoverUiState(
    val tripWithAttractionsAndLabelsList: List<TripWithAttractionsAndLabels> = listOf()
)