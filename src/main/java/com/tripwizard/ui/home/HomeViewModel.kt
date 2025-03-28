package com.tripwizard.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwizard.data.AttractionRepository
import com.tripwizard.data.Label
import com.tripwizard.data.LabelOptions
import com.tripwizard.data.LabelRepository
import com.tripwizard.data.Notification
import com.tripwizard.data.NotificationRepository
import com.tripwizard.data.Trip
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.data.TripsRepository
import com.tripwizard.data.UserSettingsRepository
import com.tripwizard.ui.STATEFLOW_TIMEOUT_MILLIS
import com.tripwizard.ui.shared.DarkModePreferredUiState
import com.tripwizard.ui.shared.NotificationsUiState
import com.tripwizard.ui.shared.UsernameUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class HomeViewModel(
    private val tripsRepository: TripsRepository,
    private val attractionRepository: AttractionRepository,
    private val labelRepository: LabelRepository,
    userSettingsRepository: UserSettingsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    //    @OptIn(ExperimentalCoroutinesApi::class)
//    val homeUiState: StateFlow<HomeUiState> =
//        tripsRepository.getAllTripsStream().map { trips ->
//            trips.map {
//                attractionRepository.getAllAttractionsStream(it).map { attractions ->
//                    Pair(it, attractions)
//                }
//            }
//        }.map { list ->
//            combine(*list.toTypedArray()) { values -> HomeUiState(values.toList()) }
//        }.flattenConcat().stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
//            initialValue = HomeUiState()
//        )
    val homeUiState: StateFlow<HomeUiState> =
        tripsRepository.getAllTripsAndAttractionsStream().map {
            HomeUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )

    val notificationsUiState: StateFlow<NotificationsUiState> =
        notificationRepository.getAllNotificationsStream().map {
            NotificationsUiState(it)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = NotificationsUiState()
        )

    val usernameUiState: StateFlow<UsernameUiState> =
        userSettingsRepository.username.map { username ->
            UsernameUiState(username)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = UsernameUiState()
        )

    val darkModePreferredUiState: StateFlow<DarkModePreferredUiState> =
        userSettingsRepository.darkModePreferred.map { darkModePreferred ->
            DarkModePreferredUiState(darkModePreferred)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DarkModePreferredUiState()
        )

    fun addNewTrip(trip: Trip, labels: List<LabelOptions>) {
        viewModelScope.launch {
            tripsRepository.insertTripWithLabels(trip, labels.map { Label(content = it) })
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

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripsRepository.updateTrip(trip)
        }
    }

    fun updateTripWithLabels(
        tripWithAttractionsAndLabels: TripWithAttractionsAndLabels,
        newLabels: List<Label>
    ) {
        viewModelScope.launch {
            tripsRepository.updateTripWithLabels(tripWithAttractionsAndLabels, newLabels)
        }
    }
    fun addNewTripWithLabelsAndAttractions(tripWithLabelsAndAttractions: TripWithAttractionsAndLabels) {
        Log.d("test", "HERE")
        viewModelScope.launch {
            tripsRepository.addNewTripWithLabelsAndAttractions(
                trip = tripWithLabelsAndAttractions.trip,
                attractions = tripWithLabelsAndAttractions.attractions,
                labels = tripWithLabelsAndAttractions.labels
            )
        }
    }

}

data class HomeUiState(
    val tripWithAttractionsAndLabelsList: List<TripWithAttractionsAndLabels> = listOf()
)