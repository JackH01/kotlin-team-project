package com.tripwizard.ui.tripdetails

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripwizard.data.Attraction
import com.tripwizard.data.AttractionRepository
import com.tripwizard.data.Label
import com.tripwizard.data.Notification
import com.tripwizard.data.NotificationRepository
import com.tripwizard.data.Trip
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.data.TripsRepository
import com.tripwizard.data.UserSettingsRepository
import com.tripwizard.ui.STATEFLOW_TIMEOUT_MILLIS
import com.tripwizard.ui.home.HomeUiState
import com.tripwizard.ui.shared.DarkModePreferredUiState
import com.tripwizard.ui.shared.NotificationsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tripsRepository: TripsRepository,
    private val attractionRepository: AttractionRepository,
    userSettingsRepository: UserSettingsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val tripId: Int =
        checkNotNull(savedStateHandle["tripId"])

    val tripUiState: StateFlow<TripUiState> =
        tripsRepository.getAllTripsAndAttractionsStream().map {
            val tripsFound = it.filter { tripWithAttractionsAndLabels ->
                tripWithAttractionsAndLabels.trip.id == tripId
            }
            if (tripsFound.isNotEmpty()) TripUiState(tripsFound[0]) else TripUiState()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = TripUiState()
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

    val attractionsUiState: (Trip?) -> StateFlow<AttractionsUiState> = { trip ->
        attractionRepository.getAllAttractionsStream(trip ?: Trip()).map { AttractionsUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
                initialValue = AttractionsUiState()
            )
    }

    val darkModePreferredUiState: StateFlow<DarkModePreferredUiState> =
        userSettingsRepository.darkModePreferred.map { darkModePreferred ->
            DarkModePreferredUiState(darkModePreferred)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_TIMEOUT_MILLIS),
            initialValue = DarkModePreferredUiState()
        )

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripsRepository.updateTrip(trip)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripsRepository.deleteTrip(trip)
        }
    }

    // Manually labels and attractions before deleting the trip
    fun deleteTripWithLabelsAndAttractions(trip: Trip) {
        viewModelScope.launch {
            tripsRepository.deleteLabelsByTripId(trip.id)
            tripsRepository.deleteAttractionsByTripId(trip.id)
            tripsRepository.deleteTrip(trip)
        }
    }

    fun updateAttraction(attraction: Attraction) {
        Log.d("editAttraction", "Updated Name: ${attraction.name}")
        Log.d("editAttraction", "Updated Description: ${attraction.description}")
        Log.d("editAttraction", "Updated Priority: ${attraction.priority}")
        viewModelScope.launch {
            attractionRepository.updateAttraction(attraction)
        }
    }

    fun deleteAttraction(attraction: Attraction) {
        viewModelScope.launch {
            attractionRepository.deleteAttraction(attraction)
        }
    }

    fun addNewAttraction(attraction: Attraction) {
        viewModelScope.launch {
            attractionRepository.insertAttraction(attraction)
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

data class TripUiState(
    val tripWithAttractionsAndLabels: TripWithAttractionsAndLabels = TripWithAttractionsAndLabels(
        Trip(id = -1), emptyArray<Attraction>().asList(
        ), emptyArray<Label>().asList()
    )
)

data class TripListUiState(
    val tripWithAttractionsAndLabelsList: List<TripWithAttractionsAndLabels> = listOf()
)

data class AttractionsUiState(
    val attractionList: List<Attraction> = emptyArray<Attraction>().asList()
)