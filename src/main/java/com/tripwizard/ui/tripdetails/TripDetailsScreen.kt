package com.tripwizard.ui.tripdetails

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tripwizard.R
import com.tripwizard.data.Attraction
import com.tripwizard.data.Priority
import com.tripwizard.data.Trip
import com.tripwizard.ui.AppViewModelProvider
import com.tripwizard.ui.components.TripWizardBottomNavigationBar
import com.tripwizard.ui.components.TripWizardTopAppBar
import com.tripwizard.ui.navigation.NavigationDestination
import com.tripwizard.ui.utils.handleNotifications
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class sortBy {
    PRIORITY,
    DATE,
    DONE
}

@Composable
fun TripDetailsScreen(
    darkModePreferredListener: (Boolean?) -> Unit,
    onNavigate: (String) -> Unit,
    navigateUp: () -> Unit,
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val darkModePreferredUiState by viewModel.darkModePreferredUiState.collectAsState()
    darkModePreferredListener(darkModePreferredUiState.darkModePreferred)

    val tripUiState by viewModel.tripUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val tripListUiState by viewModel.tripListUiState.collectAsState()

    var showNewAttractionDialog by rememberSaveable { mutableStateOf(false) }

    var sortBy by rememberSaveable { mutableStateOf(sortBy.DATE) }

    val attractions = when (sortBy) {
        com.tripwizard.ui.tripdetails.sortBy.PRIORITY -> tripUiState.tripWithAttractionsAndLabels.attractions.sortedBy { it.priority }
        com.tripwizard.ui.tripdetails.sortBy.DATE -> tripUiState.tripWithAttractionsAndLabels.attractions.sortedBy { it.date }
        com.tripwizard.ui.tripdetails.sortBy.DONE -> tripUiState.tripWithAttractionsAndLabels.attractions.sortedBy { it.done }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    handleNotifications(
        scope = scope,
        snackbarHostState = snackbarHostState,
        notificationsUiState = viewModel.notificationsUiState,
        updateNotification = viewModel::updateNotification,
        insertNotifications = viewModel::insertNotifications,
        tripsWithAttractionsAndLabelsList = tripListUiState.tripWithAttractionsAndLabelsList
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TripWizardTopAppBar(
                title = stringResource(NavigationDestination.TRIP_DETAILS.titleRes!!),
                canNavigateBack = true,
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
                notificationsUiState = viewModel.notificationsUiState,
                tripsWithAttractionsAndLabelsList = tripListUiState.tripWithAttractionsAndLabelsList,
                updateNotification = viewModel::updateNotification,
            )
        },
        bottomBar = {
            TripWizardBottomNavigationBar(
                currentRoute = NavigationDestination.TRIP_DETAILS,
                navigate = onNavigate
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewAttractionDialog = true },
                icon = { Icon(Icons.Filled.Add, "Add Attraction") },
                text = { Text(text = "Add Attraction") }
            )
        }
    ) { innerPadding ->
        TripDetailsBody(
            trip = tripUiState.tripWithAttractionsAndLabels.trip,
            deleteTripFromDb = viewModel::deleteTripWithLabelsAndAttractions,
            deleteAttractionFromDb = viewModel::deleteAttraction,
            updateAttractionInDb = viewModel::updateAttraction,
            navigateToHome = navigateToHome,
            modifier = modifier.padding(innerPadding),
            viewModel = viewModel,
            attractions = attractions,
            showNewAttractionDialog = showNewAttractionDialog,
            clickShowNewAttractionDialog = { showNewAttractionDialog = it },
            changeSortBy = {
                sortBy = if (it == sortBy) com.tripwizard.ui.tripdetails.sortBy.DATE else it
            },
            onNavigate = onNavigate
        )
    }
}

@Composable
fun TripDetailsBody(
    trip: Trip,
    deleteTripFromDb: (Trip) -> Unit,
    deleteAttractionFromDb: (Attraction) -> Unit,
    updateAttractionInDb: (Attraction) -> Unit,
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripDetailsViewModel,
    attractions: List<Attraction>,
    showNewAttractionDialog: Boolean,
    clickShowNewAttractionDialog: (Boolean) -> Unit,
    changeSortBy: (sortBy) -> Unit,
    onNavigate: (String) -> Unit,
) {
//    val attractions = attractionsUiState.attractionList
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            Column(modifier = Modifier.padding(bottom = 72.dp)) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_large))
                    ) {
                        if (trip.name.isNotEmpty()) Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trip.name,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        if (trip.description.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = trip.description,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                        }


                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${formatLocalDate(trip.start)} - ${formatLocalDate(trip.end)} " +
                                        "(${
                                            Duration.between(
                                                trip.start.atStartOfDay(),
                                                trip.end.atStartOfDay()
                                            ).toDays()
                                        } days)",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))

                        if (attractions.isNotEmpty()) {
                            AttractionList(
                                attractionsList = attractions,
                                viewModel = viewModel,
                                updateAttractionInDb = updateAttractionInDb,
                                deleteAttractionFromDb = deleteAttractionFromDb,
                                changeSortBy = changeSortBy
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(fraction = .60f)
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Add an Attraction to get started!",
                                        modifier = Modifier.padding(
                                            vertical = 32.dp,
                                            horizontal = 20.dp
                                        ),
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                        ),
                                    )
                                }
                            }
                        }

                    }
                }
                Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Card {
                    Box(contentAlignment = Alignment.TopStart) {
                        val context: Context = LocalContext.current
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
                        if (trip.id != -1) {
                            val tripLocation = LatLng(trip.latitude, trip.longitude)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(
                                    tripLocation,
                                    10f
                                )
                            }
                            GoogleMap(
                                modifier = Modifier.height(200.dp),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(isMyLocationEnabled = locationPermissionsEnabled)
                            ) {
                                Marker(
                                    state = MarkerState(position = tripLocation),
                                    title = "Trip Location",
                                    snippet = "Trip Location"
                                )
                                Circle(
                                    center = tripLocation,
                                    radius = trip.radius.toDouble()
                                )
                            }
                            FilledIconButton(
                                onClick = { onNavigate("map/${trip.latitude} ${trip.longitude}") },
                                modifier = Modifier.padding(
                                    dimensionResource(id = R.dimen.padding_small)
                                )
                            ) {
                                Icon(Icons.Filled.Map, contentDescription = "Open trip on map")
                            }
                        }
                    }
                }
            }



            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                DeleteTripButton(
                    trip = trip,
                    deleteTripFromDb = deleteTripFromDb,
                    navigateToHome = navigateToHome,
                )
            }
        }
    }
    if (showNewAttractionDialog) {
        NewAttraction(
            addNewAttractionToDb = viewModel::addNewAttraction,
            onDismissRequest = { clickShowNewAttractionDialog(false) },
            currentTrip = trip
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttractionList(
    attractionsList: List<Attraction>,
    modifier: Modifier = Modifier,
    viewModel: TripDetailsViewModel,
    updateAttractionInDb: (Attraction) -> Unit,
    deleteAttractionFromDb: (Attraction) -> Unit,
    changeSortBy: (sortBy) -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight(fraction = .60f)) {
        LazyColumn(modifier = modifier) {
            stickyHeader {
                Surface(
                    Modifier
                        .fillParentMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        Column(
                            modifier = modifier
                                .weight(1f)
                                .clickable { changeSortBy(sortBy.PRIORITY) },
                        ) {
                            Text(stringResource(R.string.attraction_col_header_priority))
                        }
                        Column(
                            modifier = modifier
                                .weight(4f),
                        ) {
                            Text(stringResource(R.string.attraction_col_header_name))
                        }
                        Column(
                            modifier = modifier
                                .weight(3f)
                                .clickable { changeSortBy(sortBy.DATE) },
                        ) {
                            Text(stringResource(R.string.attraction_col_header_date))
                        }
                        Column(
                            modifier = modifier
                                .weight(2f)
                                .clickable { changeSortBy(sortBy.DONE) },
                        ) {
                            Text(stringResource(R.string.attraction_col_header_done))
                        }
                        Column(
                            modifier = modifier
                                .weight(1.5f),
                        ) {
                            Text(stringResource(R.string.attraction_col_header_edit))
                        }
                        Column(
                            modifier = modifier
                                .weight(1.5f),
                        ) {
                            Text(stringResource(R.string.attraction_col_header_del))
                        }
                    }
                }
            }

            items(items = attractionsList, key = { it.id }) { attraction ->
                AttractionItem(
                    attraction = attraction,
                    modifier = Modifier
                        .padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                    viewModel = viewModel,
                    updateAttractionInDb = updateAttractionInDb,
                    deleteAttractionFromDb = deleteAttractionFromDb
                )
            }
        }
    }

}

@Composable
fun AttractionItem(
    attraction: Attraction,
    modifier: Modifier = Modifier,
    viewModel: TripDetailsViewModel,
    updateAttractionInDb: (Attraction) -> Unit,
    deleteAttractionFromDb: (Attraction) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_small))
            .clickable {
                expanded = !expanded
            }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = modifier
                    .weight(1f),
            ) {
                //get the dark mode state
                val darkModePreferred by viewModel.darkModePreferredUiState.collectAsState()
                val darkModeValue: Boolean? = darkModePreferred.darkModePreferred

                // set default priority icon colours for light mode
                val tints = hashMapOf(
                    "low" to Color.Blue,
                    "med" to Color.Black,
                    "high" to Color.Red
                )

                // adjust priority icon colours for dark mode
                if (darkModeValue != null) {
                    if (darkModeValue) {
                        tints["low"] = MaterialTheme.colorScheme.tertiary
                        tints["med"] = MaterialTheme.colorScheme.primary
                        tints["high"] = MaterialTheme.colorScheme.error
                    }
                }

                // priority Icons
                when (attraction.priority) {
                    Priority.LOW -> tints["low"]?.let {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            "Low Priority",
                            tint = it
                        )
                    }

                    Priority.MEDIUM -> tints["med"]?.let {
                        Icon(
                            Icons.Filled.KeyboardArrowRight,
                            "Medium Priority",
                            tint = it
                        )
                    }

                    else -> tints["high"]?.let {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            "High Priority",
                            tint = it
                        )
                    }
                }
            }
            Column(
                Modifier.weight(4f)
            ) {
                if (attraction.done) {
                    Text(
                        text = attraction.name,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.LineThrough
                    )
                } else {
                    Text(
                        text = attraction.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Column(
                Modifier.weight(2f)
            ) {
                Text(formatLocalDateDaysMonth(attraction.date))
            }
            Column(
                Modifier.weight(2f)
            ) {
                Checkbox(checked = attraction.done,
                    onCheckedChange = {
                        viewModel.updateAttraction(attraction.copy(done = it))
                    }
                )
            }
            Column(
                Modifier.weight(1.5f)
            ) {
                EditAttractionButton(
                    attraction = attraction,
                    updateAttractionInDb = updateAttractionInDb
                )
            }
            Column(
                Modifier.weight(1.5f)
            ) {
                DeleteAttractionButton(
                    attraction = attraction,
                    deleteAttractionFromDb = deleteAttractionFromDb
                )
            }
        }
        AnimatedVisibility(
            expanded && attraction.description.isNotBlank(), enter = expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(
                initialAlpha = 0.3f
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            Row() {
                Column(modifier = modifier.weight(1f)) {}
                Column(
                    Modifier.weight(8f)
                ) {
                    Text(
                        text = attraction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                    )
                }
                Column(Modifier.weight(1.5f)) {}
            }
        }
    }
}

@Composable
fun DeleteTripButton(trip: Trip, deleteTripFromDb: (Trip) -> Unit, navigateToHome: () -> Unit) {
    var showConfirmDeleteTripDialog by remember { mutableStateOf(false) }
    FilledIconButton(onClick = {
        showConfirmDeleteTripDialog = true
    }
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(R.string.delete_trip_button_description),
            tint = Color.White
        )
        if (showConfirmDeleteTripDialog) {
            ConfirmDeleteTripDialog(
                onConfirm = {
                    deleteTripFromDb(trip)
                    navigateToHome()
                },
                onCancel = {
                    showConfirmDeleteTripDialog = false
                }
            )
        }
    }
}

@Composable
fun DeleteAttractionButton(attraction: Attraction, deleteAttractionFromDb: (Attraction) -> Unit) {
    var showConfirmDeleteAttractionDialog by remember { mutableStateOf(false) }
    FilledIconButton(
        onClick = {
            showConfirmDeleteAttractionDialog = true
        },
        modifier = Modifier.size(30.dp),
        colors = IconButtonColors(Color.Red, Color.Red, Color.Red, Color.Red)
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(R.string.delete_trip_button_description),
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        if (showConfirmDeleteAttractionDialog) {
            ConfirmDeleteAttractionDialog(
                onConfirm = {
                    deleteAttractionFromDb(attraction)
                },
                onCancel = {
                    showConfirmDeleteAttractionDialog = false
                }
            )
        }
    }
}

@Composable
fun EditAttractionButton(
    attraction: Attraction,
    updateAttractionInDb: (Attraction) -> Unit
) {
    var showEditAttraction by remember { mutableStateOf(false) }
    FilledIconButton(
        onClick = {
            showEditAttraction = true
        },
        modifier = Modifier.size(30.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.delete_trip_button_description),
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
    if (showEditAttraction) {
        EditAttraction(
            updateAttractionInDb = updateAttractionInDb,
            onDismissRequest = { showEditAttraction = false },
            currentAttraction = attraction
        )
    }
}

@Composable
fun ConfirmDeleteAttractionDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.delete_attraction_confirmation)) },
        text = { Text(stringResource(R.string.delete_attraction_message)) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onCancel()
                }
            ) {
                Text(stringResource(R.string.delete_btn))
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel
            ) {
                Text(stringResource(R.string.cancel_btn))
            }
        }
    )
}

@Composable
fun ConfirmDeleteTripDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.delete_trip_confirmation)) },
        text = { Text(stringResource(R.string.delete_trip_message)) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onCancel()
                }
            ) {
                Text(stringResource(R.string.delete_btn))
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel
            ) {
                Text(stringResource(R.string.cancel_btn))
            }
        }
    )
}

// TODO: Move to Converters class
fun formatLocalDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
}

fun formatLocalDateDaysMonth(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd/MM"))
}