package com.tripwizard.ui.usersettings

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
import com.tripwizard.ui.shared.UsernameUiState
import com.tripwizard.ui.tripdetails.TripListUiState
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

class UserSettingsViewModel(
    private val tripsRepository: TripsRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    val usernameUiState: StateFlow<DataState<UsernameUiState>> =
        userSettingsRepository.username.map { username ->
            DataState(UsernameUiState(username), false)
        }.onStart { emit(DataState(UsernameUiState(), true)) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DataState(UsernameUiState(), true)
        )

    val tripListUiState: StateFlow<TripListUiState> =
        tripsRepository.getAllTripsAndAttractionsStream().map {
            TripListUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = TripListUiState()
        )

    val notificationsUiState: StateFlow<NotificationsUiState> =
        notificationRepository.getAllNotificationsStream().map {
            NotificationsUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = NotificationsUiState()
        )

    suspend fun updateUsername(username: String) {
        userSettingsRepository.saveUsername(username)
    }

    val darkModePreferredUiState: StateFlow<DataState<DarkModePreferredUiState>> =
        userSettingsRepository.darkModePreferred.map { darkModePreferred ->
            DataState(DarkModePreferredUiState(darkModePreferred), false)
        }.onStart { emit(DataState(DarkModePreferredUiState(), true)) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DataState(DarkModePreferredUiState(), true)
        )

    fun updateDarkModePreferred(darkModePreferred: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.saveDarkModePreferred(darkModePreferred)
        }
    }

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
