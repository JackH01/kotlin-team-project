package com.tripwizard.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tripwizard.ui.AppViewModelProvider
import com.tripwizard.ui.components.TripWizardBottomNavigationBar
import com.tripwizard.ui.components.TripWizardTopAppBar
import com.tripwizard.ui.navigation.NavigationDestination
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import com.google.maps.android.compose.Circle
import com.tripwizard.ui.shared.Coordinates
import com.tripwizard.ui.utils.getLocation
import com.tripwizard.ui.utils.handleNotifications
import com.tripwizard.ui.utils.hasCoarseLocationPermission
import com.tripwizard.ui.utils.hasFineLocationPermission

@Composable
fun MapScreen(
    darkModePreferredListener: (Boolean?) -> Unit,
    onNavigate: (String) -> Unit,
    navigateUp: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val darkModePreferredUiState by viewModel.darkModePreferredUiState.collectAsState()
    darkModePreferredListener(darkModePreferredUiState.darkModePreferred)
    //val latestUserLocationUiState by viewModel.latestUserLocationUiState.collectAsState()
    //val mapUiState by viewModel.mapUiState.collectAsState()

    val context = LocalContext.current
    var permissionCoarseLocation by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    var permissionFineLocation by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    val requestPermissions =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                permissionCoarseLocation = PackageManager.PERMISSION_GRANTED
            }
            if (it[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                permissionFineLocation = PackageManager.PERMISSION_GRANTED
            }
        }

    SideEffect {
        if (permissionCoarseLocation == PackageManager.PERMISSION_DENIED && permissionFineLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    val locationPermissionsEnabled =
        permissionCoarseLocation == PackageManager.PERMISSION_GRANTED || permissionFineLocation ==
                PackageManager.PERMISSION_GRANTED

    MapScreenInner(
        onNavigate = onNavigate,
        navigateUp = navigateUp,
        locationPermissionsEnabled = locationPermissionsEnabled,
        viewModel = viewModel
    )
}

//@RequiresApi(Build.VERSION_CODES.R)
//fun getCurrentFineLocation(context: Context, callback: (Location?) -> Unit) {
//    val locationManager =
//        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//    try {
//        locationManager.getCurrentLocation(
//            LocationManager.GPS_PROVIDER,
//            null,
//            context.mainExecutor,
//            callback,
//        )
//    } catch (e: SecurityException) {
//        Log.e("Exception: %s", e.message, e)
//    }
//}

@Composable
fun MapScreenInner(
    onNavigate: (String) -> Unit,
    navigateUp: () -> Unit,
    locationPermissionsEnabled: Boolean,
    viewModel: MapViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    //val latestUserLocationUiState by viewModel.latestUserLocationUiState.collectAsState()
    val mapUiState by viewModel.mapUiState.collectAsState()

    val locationUndefined = viewModel.initialCoordinates == "undefined"

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    handleNotifications(
        scope = scope,
        snackbarHostState = snackbarHostState,
        notificationsUiState = viewModel.notificationsUiState,
        updateNotification = viewModel::updateNotification,
        insertNotifications = viewModel::insertNotifications,
        tripsWithAttractionsAndLabelsList = mapUiState.tripWithAttractionsAndLabelsList
    )

    if (!locationUndefined) {
        val parsedCoordinates = viewModel.initialCoordinates.split(" ")
        val initialLocation =
            LatLng(parsedCoordinates[0].toDouble(), parsedCoordinates[1].toDouble())

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialLocation, 10f)
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TripWizardTopAppBar(
                    title = stringResource(NavigationDestination.MAP.titleRes),
                    canNavigateBack = true,
                    navigateUp = navigateUp,
                    scrollBehavior = scrollBehavior,
                    notificationsUiState = viewModel.notificationsUiState,
                    tripsWithAttractionsAndLabelsList = mapUiState.tripWithAttractionsAndLabelsList,
                    updateNotification = viewModel::updateNotification
                )
            },
            bottomBar = {
                TripWizardBottomNavigationBar(
                    currentRoute = NavigationDestination.MAP,
                    navigate = onNavigate
                )
            }) { innerPadding ->
            GoogleMap(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionsEnabled)
            ) {
                mapUiState.tripWithAttractionsAndLabelsList.forEach { tripWithAttractionsAndLabels ->
                    val markerLocation = LatLng(
                        tripWithAttractionsAndLabels.trip.latitude,
                        tripWithAttractionsAndLabels.trip.longitude
                    )
                    val title = if (tripWithAttractionsAndLabels.trip.name.isEmpty()) {
                        "Trip ${tripWithAttractionsAndLabels.trip.id}"
                    } else {
                        tripWithAttractionsAndLabels.trip.name
                    }
                    Marker(
                        state = MarkerState(position = markerLocation),
                        title = title
                    )
                    Circle(
                        center = markerLocation,
                        radius = tripWithAttractionsAndLabels.trip.radius.toDouble()
                    )
                }
            }
        }
    } else {
        var initialLocationSet by rememberSaveable { mutableStateOf(false) }
        var initialLatitude by rememberSaveable { mutableStateOf(53.4) }
        var initialLongitude by rememberSaveable { mutableStateOf(-1.45) }
        if (locationPermissionsEnabled && !initialLocationSet) {
            getLocation { location ->
                if (location != null) {
                    initialLatitude = location.latitude
                    initialLongitude = location.longitude
                    initialLocationSet = true
                }
            }
        }

        val initialLocation =
            LatLng(initialLatitude, initialLongitude)

        val cameraPositionState = if (!initialLocationSet) (rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                initialLocation,
                10f
            )
        }
                ) else (rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialLocation, 10f)
        })


        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TripWizardTopAppBar(
                    title = stringResource(NavigationDestination.MAP.titleRes),
                    canNavigateBack = true,
                    navigateUp = navigateUp,
                    scrollBehavior = scrollBehavior,
                    notificationsUiState = viewModel.notificationsUiState,
                    tripsWithAttractionsAndLabelsList = mapUiState.tripWithAttractionsAndLabelsList,
                    updateNotification = viewModel::updateNotification
                )
            },
            bottomBar = {
                TripWizardBottomNavigationBar(
                    currentRoute = NavigationDestination.MAP,
                    navigate = onNavigate
                )
            }) { innerPadding ->
            GoogleMap(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionsEnabled)
            ) {
                mapUiState.tripWithAttractionsAndLabelsList.forEach { tripWithAttractionsAndLabels ->
                    val markerLocation = LatLng(
                        tripWithAttractionsAndLabels.trip.latitude,
                        tripWithAttractionsAndLabels.trip.longitude
                    )
                    val title = if (tripWithAttractionsAndLabels.trip.name.isEmpty()) {
                        "Trip ${tripWithAttractionsAndLabels.trip.id}"
                    } else {
                        tripWithAttractionsAndLabels.trip.name
                    }
                    Marker(
                        state = MarkerState(position = markerLocation),
                        title = title
                    )
                    Circle(
                        center = markerLocation,
                        radius = tripWithAttractionsAndLabels.trip.radius.toDouble()
                    )
                }
            }
        }
    }
}