package com.tripwizard.ui.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.tripwizard.data.Notification
import com.tripwizard.data.NotificationType
import com.tripwizard.data.Trip
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.ui.shared.Coordinates
import com.tripwizard.ui.shared.NotificationsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

const val earthRadiusKm = 6372.8
const val thresholdInKmForLocationNotification = 50.0

@Composable
fun handleNotifications(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    notificationsUiState: StateFlow<NotificationsUiState>,
    updateNotification: (Notification) -> Unit,
    insertNotifications: (List<Notification>) -> Unit,
    tripsWithAttractionsAndLabelsList: List<TripWithAttractionsAndLabels>,
    daysBeforeToRemind: Int = 1
) {
    val notifications by notificationsUiState.collectAsState()

    tripsWithAttractionsAndLabelsList.forEach {
        if (!notifications.notificationsList.map { it.tripId }.contains(it.trip.id)) {
            createNotificationsForNewTrip(it.trip, insertNotifications)
        }
    }

//    val currentDate = LocalDate.now()
//    tripsWithAttractionsAndLabelsList.forEach { outer ->
//        val reminderDate = outer.trip.start.minusDays(daysBeforeToRemind.toLong())
//        if (currentDate.isEqual(reminderDate)) {
//            val filtered = notifications.notificationsList.filter {
//                it.tripId == outer.trip.id && it.type == NotificationType.TIME && !it.shownToUser
//            }
//            if (filtered.isNotEmpty() && !filtered[0].shownToUser) {
//                showTimeNotification(outer.trip, scope, snackbarHostState)
//                updateNotification(filtered[0].copy(shownToUser = true))
//            }
//        }
//    }
    val currentDate = LocalDate.now()

    tripsWithAttractionsAndLabelsList.forEach { outer ->
        val reminderStartDate = outer.trip.start.minusDays(outer.trip.daysBeforeToRemind.toLong())

        // Check if current date is in the reminder period
        if ((currentDate.isEqual(reminderStartDate) || currentDate.isAfter(reminderStartDate))
            && (currentDate.isEqual(outer.trip.start) || currentDate.isBefore(outer.trip.start))) {

            val filtered = notifications.notificationsList.filter {
                it.tripId == outer.trip.id && it.type == NotificationType.TIME && !it.shownToUser
            }

            if (filtered.isNotEmpty()) {
                val isTripActive = currentDate.isAfter(outer.trip.start) || currentDate.isEqual(outer.trip.start)
                val message = if (isTripActive) {
                    if (outer.trip.name.isNotEmpty()) "Trip: ${outer.trip.name} is currently active" else "Trip: ${outer.trip.id} is currently active"
                } else {
                    if (outer.trip.name.isNotEmpty()) "Upcoming trip: ${outer.trip.name}" else "Upcoming trip: ${outer.trip.id}"
                }

                showTimeNotification(message, scope, snackbarHostState)
                updateNotification(filtered[0].copy(shownToUser = true))
            }
        }
    }

    val context = LocalContext.current

    var permissionCoarseLocation by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    var permissionFineLocation by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    val requestPermissions =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                permissionCoarseLocation = PackageManager.PERMISSION_GRANTED
            }
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                permissionFineLocation = PackageManager.PERMISSION_GRANTED
            }
        }

    SideEffect {
        if (permissionCoarseLocation == PackageManager.PERMISSION_DENIED && permissionFineLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }
    val locationPermissionsEnabled =
        permissionCoarseLocation == PackageManager.PERMISSION_GRANTED || permissionFineLocation ==
                PackageManager.PERMISSION_GRANTED
    var locationSet by rememberSaveable { mutableStateOf(false) }
    var latitude by rememberSaveable { mutableStateOf(0.0) }
    var longitude by rememberSaveable { mutableStateOf(0.0) }
    if (locationPermissionsEnabled && !locationSet) {
        getLocation { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                locationSet = true
            }
        }
    }

    if (locationSet) {
        tripsWithAttractionsAndLabelsList.forEach { outer ->
            val distanceToTripInKm = haversine(
                Coordinates(latitude, longitude),
                Coordinates(outer.trip.latitude, outer.trip.longitude)
            )
            if (distanceToTripInKm <= outer.trip.radius) {
                val filtered =
                    notifications.notificationsList.filter { it.tripId == outer.trip.id && it.type == NotificationType.LOCATION }
                if (filtered.isNotEmpty() && !filtered[0].shownToUser) {
                    showLocationNotification(
                        outer.trip,
                        scope,
                        snackbarHostState,
                        distanceToTripInKm
                    )
                    updateNotification(filtered[0].copy(shownToUser = true))
                }
            }
        }
    }
}

fun createNotificationsForNewTrip(trip: Trip, insertNotifications: (List<Notification>) -> Unit) {
    val timeNotification = Notification(
        tripId = trip.id,
        type = NotificationType.TIME
    )
    val locationNotification = Notification(
        tripId = trip.id,
        type = NotificationType.LOCATION
    )
    insertNotifications(listOf(timeNotification, locationNotification))
}

//fun showTimeNotification(
//    trip: Trip, scope: CoroutineScope,
//    snackbarHostState: SnackbarHostState,
//) {
//    scope.launch {
//        snackbarHostState
//            .showSnackbar(
//                message = if (trip.name.isNotEmpty()) "Trip: ${trip.name} is currently active" else "Trip: ${trip.id} is currently active",
//                duration = SnackbarDuration.Indefinite,
//                withDismissAction = true
//            )
//    }
//}
fun showTimeNotification(
    message: String,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    scope.launch {
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Indefinite,
            withDismissAction = true
        )
    }
}


fun showLocationNotification(
    trip: Trip, scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    distanceInKm: Double
) {
    scope.launch {
        snackbarHostState
            .showSnackbar(
                message = if (trip.name.isNotEmpty()) "Trip: ${trip.name} is close by (${
                    String.format(
                        "%.1f",
                        distanceInKm
                    )
                } km)" else "Trip: ${trip.id} is close by (${
                    String.format(
                        "%.1f",
                        distanceInKm
                    )
                } km)",
                duration = SnackbarDuration.Indefinite,
                withDismissAction = true
            )
    }
}

fun haversine(firstLocation: Coordinates, secondLocation: Coordinates): Double {
    val dLat = Math.toRadians(firstLocation.latitude - secondLocation.latitude);
    val dLon = Math.toRadians(firstLocation.longitude - secondLocation.longitude);
    val firstLat = Math.toRadians(firstLocation.latitude);
    val secondLat = Math.toRadians(secondLocation.latitude);

    val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(
        Math.sin(dLon / 2),
        2.toDouble()
    ) * Math.cos(firstLat) * Math.cos(secondLat);
    val c = 2 * Math.asin(Math.sqrt(a));
    return earthRadiusKm * c;
}