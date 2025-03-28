package com.tripwizard.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwizard.data.AttractionRepository
import com.tripwizard.data.Notification
import com.tripwizard.data.NotificationRepository
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.data.TripsRepository
import com.tripwizard.data.UserSettingsRepository
import com.tripwizard.ui.STATEFLOW_TIMEOUT_MILLIS
import com.tripwizard.ui.home.HomeUiState
import com.tripwizard.ui.shared.DarkModePreferredUiState
import com.tripwizard.ui.shared.LatestUserLocationUiState
import com.tripwizard.ui.shared.NotificationsUiState
import com.tripwizard.ui.shared.UsernameUiState
import com.tripwizard.ui.usersettings.DataState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DataState<T>(
    val data: T,
    val isLoading: Boolean
)

class MapViewModel(
    savedStateHandle: SavedStateHandle,
    private val tripsRepository: TripsRepository,
    userSettingsRepository: UserSettingsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val initialCoordinates: String =
        checkNotNull(savedStateHandle["initialCoordinates"])

    val notificationsUiState: StateFlow<NotificationsUiState> =
        notificationRepository.getAllNotificationsStream().map {
            NotificationsUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = NotificationsUiState()
        )

    val mapUiState: StateFlow<MapUiState> =
        tripsRepository.getAllTripsAndAttractionsStream().map {
            MapUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = MapUiState()
        )
    val darkModePreferredUiState: StateFlow<DarkModePreferredUiState> =
        userSettingsRepository.darkModePreferred.map { darkModePreferred ->
            DarkModePreferredUiState(darkModePreferred)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DarkModePreferredUiState()
        )

    val latestUserLocationUiState: StateFlow<DataState<LatestUserLocationUiState>> =
        userSettingsRepository.latestUserLocation.map { location ->
            DataState(LatestUserLocationUiState(location), false)
        }.onStart { emit(DataState(LatestUserLocationUiState(), true)) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DataState(LatestUserLocationUiState(), true)
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
}

data class MapUiState(
    val tripWithAttractionsAndLabelsList: List<TripWithAttractionsAndLabels> = listOf()
)
