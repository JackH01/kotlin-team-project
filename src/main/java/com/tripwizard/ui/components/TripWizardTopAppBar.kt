package com.tripwizard.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Doorbell
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import com.tripwizard.R
import com.tripwizard.data.LabelOptions
import com.tripwizard.data.Notification
import com.tripwizard.data.NotificationType
import com.tripwizard.data.Trip
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.ui.shared.Coordinates
import com.tripwizard.ui.shared.NotificationsUiState
import com.tripwizard.ui.utils.getLocation
import com.tripwizard.ui.utils.haversine
import com.tripwizard.ui.utils.showLocationNotification
import com.tripwizard.ui.utils.showTimeNotification
import com.tripwizard.ui.utils.thresholdInKmForLocationNotification
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

@Composable
fun TripWizardTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    notificationsUiState: StateFlow<NotificationsUiState>,
    tripsWithAttractionsAndLabelsList: List<TripWithAttractionsAndLabels>,
    updateNotification: (Notification) -> Unit
) {
    val notifications by notificationsUiState.collectAsState()

    var unreadNotifications = listOf<Pair<Notification, Trip>>()

    val currentDate = LocalDate.now()
    tripsWithAttractionsAndLabelsList.forEach { outer ->
        val reminderStartDate = outer.trip.start.minusDays(outer.trip.daysBeforeToRemind.toLong())

        // Check if current date is in the reminder period
        if ((currentDate.isEqual(reminderStartDate) || currentDate.isAfter(reminderStartDate))
            && (currentDate.isEqual(outer.trip.start) || currentDate.isBefore(outer.trip.start))
        ) {

            val filtered =
                notifications.notificationsList.filter { it.tripId == outer.trip.id && it.type == NotificationType.TIME }

            if (filtered.isNotEmpty()) {
                if (filtered.isNotEmpty() && filtered[0].unread && !unreadNotifications.contains(
                        Pair(
                            filtered[0],
                            outer.trip
                        )
                    )
                ) {
                    unreadNotifications = unreadNotifications + Pair(filtered[0], outer.trip)
                }
            }
        }
//        if (currentDate.isAfter(outer.trip.start) && currentDate.isBefore(outer.trip.end)) {
//            val filtered =
//                notifications.notificationsList.filter { it.tripId == outer.trip.id && it.type == NotificationType.TIME }
//            if (filtered.isNotEmpty() && filtered[0].unread && !unreadNotifications.contains(
//                    Pair(
//                        filtered[0],
//                        outer.trip
//                    )
//                )
//            ) {
//                unreadNotifications = unreadNotifications + Pair(filtered[0], outer.trip)
//            }
//        }
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
                if (filtered.isNotEmpty() && filtered[0].unread && !unreadNotifications.contains(
                        Pair(filtered[0], outer.trip)
                    )
                ) {
                    unreadNotifications = unreadNotifications + Pair(filtered[0], outer.trip)
                }
            }
        }
    }

    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = {
            if (unreadNotifications.isNotEmpty()) {
                var expanded by rememberSaveable { mutableStateOf(false) }

                BadgedBox(
                    badge = {
                        Badge {
                            val badgeNumber = unreadNotifications.size.toString()
                            Text(
                                badgeNumber,
                                modifier = Modifier.semantics {
                                    contentDescription = "$badgeNumber new notifications"
                                }
                            )
                        }
                    }) {
                    FilledIconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Filled.NotificationsActive, "Expand/Collapse Notifications")
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                    for (notification in unreadNotifications) {
                        if (notification.first.type == NotificationType.TIME) {
                            val isTripActive =
                                currentDate.isAfter(notification.second.start) || currentDate.isEqual(
                                    notification.second.start
                                )
                            val message = if (isTripActive) {
                                if (notification.second.name.isNotEmpty()) "Trip: ${notification.second.name} is currently active" else "Trip: ${notification.second.id} is currently active"
                            } else {
                                if (notification.second.name.isNotEmpty()) "Upcoming trip: ${notification.second.name}" else "Upcoming trip: ${notification.second.id}"
                            }
                            DropdownMenuItem(
                                text = { Text(text = message) },
                                onClick = {
                                    expanded = false
                                    updateNotification(notification.first.copy(unread = false))
                                }
                            )
                        } else {
                            val distanceInKm = haversine(
                                Coordinates(latitude, longitude),
                                Coordinates(
                                    notification.second.latitude,
                                    notification.second.longitude
                                )
                            )
                            val message =
                                if (notification.second.name.isNotEmpty()) "Trip: ${notification.second.name} is close by (${
                                    String.format(
                                        "%.1f",
                                        distanceInKm
                                    )
                                } km)" else "Trip: ${notification.second.id} is close by (${
                                    String.format(
                                        "%.1f",
                                        distanceInKm
                                    )
                                } km)"
                            DropdownMenuItem(
                                text = { Text(text = message) },
                                onClick = {
                                    expanded = false
                                    updateNotification(notification.first.copy(unread = false))
                                }
                            )
                        }
                    }
                }
            } else {
                var expanded by rememberSaveable { mutableStateOf(false) }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.Notifications, "Expand/Collapse Notifications")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(text = "No new notifications...") },
                        onClick = {
                            expanded = false
                        },
                        enabled = false
                    )
                }
            }
        },
    )
}