package com.tripwizard.ui.home

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripwizard.R
import com.tripwizard.data.Label
import com.tripwizard.data.LabelOptions
import com.tripwizard.data.Trip
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.ui.AppViewModelProvider
import com.tripwizard.ui.components.TripWizardBottomNavigationBar
import com.tripwizard.ui.components.TripWizardTopAppBar
import com.tripwizard.ui.navigation.NavigationDestination
import com.tripwizard.ui.shared.Coordinates
import com.tripwizard.ui.utils.getLocation
import com.tripwizard.ui.utils.handleNotifications
import com.tripwizard.ui.utils.haversine
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class SortBy {
    TIME,
    LOCATION
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun HomeScreen(
    darkModePreferredListener: (Boolean?) -> Unit,
    onNavigate: (String) -> Unit,
    navigateToTripDetailsView: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val darkModePreferredUiState by viewModel.darkModePreferredUiState.collectAsState()
    darkModePreferredListener(darkModePreferredUiState.darkModePreferred)

    val homeUiState by viewModel.homeUiState.collectAsState()
    val usernameUiState by viewModel.usernameUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var showNewTripDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showEditTripDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var tripCurrentlyBeingEdited by rememberSaveable {
        mutableStateOf(0)
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    handleNotifications(
        scope = scope,
        snackbarHostState = snackbarHostState,
        notificationsUiState = viewModel.notificationsUiState,
        updateNotification = viewModel::updateNotification,
        insertNotifications = viewModel::insertNotifications,
        tripsWithAttractionsAndLabelsList = homeUiState.tripWithAttractionsAndLabelsList
    )

    var sortBy: SortBy by rememberSaveable {
        mutableStateOf(SortBy.TIME)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TripWizardTopAppBar(
                title = stringResource(NavigationDestination.HOME.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior,
                notificationsUiState = viewModel.notificationsUiState,
                tripsWithAttractionsAndLabelsList = homeUiState.tripWithAttractionsAndLabelsList,
                updateNotification = viewModel::updateNotification,
            )
        },
        bottomBar = {
            TripWizardBottomNavigationBar(
                currentRoute = NavigationDestination.HOME,
                navigate = onNavigate
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewTripDialog = true },
                icon = { Icon(Icons.Filled.Add, stringResource(id = R.string.add_trip)) },
                text = { Text(text = "Add Trip") }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (usernameUiState.username.isNotEmpty()) {
                Text(
                    text = "Hello, ${usernameUiState.username}!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
                )
            }
            Box(contentAlignment = Alignment.BottomStart) {
                HomeBody(
                    tripAttractionList = homeUiState.tripWithAttractionsAndLabelsList,
                    onItemClick = navigateToTripDetailsView,
                    handleEditButtonClick = {
                        tripCurrentlyBeingEdited = it.id
                        showEditTripDialog = true
                    },
                    onNavigate = onNavigate,
                    sortBy = sortBy,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_medium))
                        .padding(
                            horizontal = max(
                                innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                                innerPadding.calculateStartPadding(LayoutDirection.Rtl)
                            )
                        )
                ) {
                    Text(text = "Sort by:")
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    if (sortBy == SortBy.TIME) {
                        ElevatedButton(onClick = { sortBy = SortBy.LOCATION }) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null)
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                            Text("Time")
                        }
                    } else if (sortBy == SortBy.LOCATION) {
                        ElevatedButton(onClick = { sortBy = SortBy.TIME }) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                            Text("Location")
                        }
                    }
                }
            }
        }
    }

    if (showNewTripDialog) {
        NewTrip(
            addNewTripToDb = viewModel::addNewTrip,
            onDismissRequest = { showNewTripDialog = false }
        )
    }

    if (showEditTripDialog) {
        var initialName = ""
        var initialDescription = ""
        var initialStart = LocalDate.now()
        var initialEnd = LocalDate.now()
        var initialLatitude = 0.0
        var initialLongitude = 0.0
        var initialRadius = 0f
        var initialDaysBeforeToRemind = 1
        var initialImageUri: Uri? = null
        var initialLabelOptions = listOf<LabelOptions>()
        homeUiState.tripWithAttractionsAndLabelsList.find { it.trip.id == tripCurrentlyBeingEdited }
            ?.let {
                initialName = it.trip.name
                initialDescription = it.trip.description
                initialStart = it.trip.start
                initialEnd = it.trip.end
                initialLatitude = it.trip.latitude
                initialLongitude = it.trip.longitude
                initialRadius = it.trip.radius
                initialDaysBeforeToRemind = it.trip.daysBeforeToRemind
                initialImageUri = Uri.parse(it.trip.imageUri)
                initialLabelOptions = it.labels.map { label -> label.content }
            }
        val updateTripInDb: (name: String, description: String, start: LocalDate, end: LocalDate, latitude: Double, longitude: Double, radius: Float, daysBeforeToRemind: Int, imageUri: String, labelOptions: List<LabelOptions>) -> Unit =
            { name, description, start, end, latitude, longitude, radius, daysBeforeToRemind, imageUri, labelOptions ->
                homeUiState.tripWithAttractionsAndLabelsList.find { it.trip.id == tripCurrentlyBeingEdited }
                    ?.let {
                        val newTripWithAttractionsAndLabels = it.copy(
                            trip = it.trip.copy(
                                name = name,
                                description = description,
                                start = start,
                                end = end,
                                latitude = latitude,
                                longitude = longitude,
                                radius = radius,
                                daysBeforeToRemind = daysBeforeToRemind,
                                imageUri = imageUri
                            )
                        )
                        viewModel.updateTripWithLabels(
                            newTripWithAttractionsAndLabels,
                            labelOptions.map { label -> Label(content = label) })
                    }
                showEditTripDialog = false
            }
        EditTrip(
            initialName = initialName,
            initialDescription = initialDescription,
            initialStart = initialStart,
            initialEnd = initialEnd,
            initialLatitude = initialLatitude,
            initialLongitude = initialLongitude,
            initialRadius = initialRadius,
            initialDaysBeforeToRemind = initialDaysBeforeToRemind,
            initialLabelOptions = initialLabelOptions,
            initialImageUri = initialImageUri,
            updateTripInDb = updateTripInDb,
            onDismissRequest = { showEditTripDialog = false }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
private fun HomeBody(
    tripAttractionList: List<TripWithAttractionsAndLabels>,
    onItemClick: (Int) -> Unit,
    handleEditButtonClick: (trip: Trip) -> Unit,
    onNavigate: (String) -> Unit,
    sortBy: SortBy,
    modifier: Modifier = Modifier
) {
    if (tripAttractionList.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.no_trips_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            TripList(
                tripAttractionList = tripAttractionList,
                onItemClick = { onItemClick(it.id) },
                handleEditButtonClick = handleEditButtonClick,
                onNavigate = onNavigate,
                sortBy = sortBy,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun TripList(
    tripAttractionList: List<TripWithAttractionsAndLabels>,
    onItemClick: (Trip) -> Unit,
    handleEditButtonClick: (trip: Trip) -> Unit,
    onNavigate: (String) -> Unit,
    sortBy: SortBy,
    modifier: Modifier = Modifier
) {
    if (sortBy == SortBy.TIME) {
        val partitioned = tripAttractionList.partition { it.trip.end.isBefore(LocalDate.now()) }
        val pastTrips = partitioned.first
        val currentOrFutureTrips = partitioned.second
        val sortedPastTrips = pastTrips.sortedBy { it.trip.start }
        val sortedCurrentOrFutureTrips = currentOrFutureTrips.sortedBy { it.trip.start }
        val groupedCurrentOrFutureTrips = sortedCurrentOrFutureTrips.groupBy { it.trip.start }
        var pastTripsExpanded by rememberSaveable { mutableStateOf(false) }

        var entered by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            entered = true
        }
        AnimatedVisibility(
            entered, enter = expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(
                initialAlpha = 0.3f
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            LazyColumn(
                modifier = modifier
                    .padding(bottom = 82.dp)
            ) {
                itemsIndexed(items = groupedCurrentOrFutureTrips.map { it.value }) { index, tripWithAttractionsSubList ->
                    val datesDiff = Duration.between(
                        LocalDate.now().atStartOfDay(),
                        tripWithAttractionsSubList[0].trip.start.atStartOfDay()
                    ).toDays()
                    Text(
                        text = if (datesDiff > 0) "In ${
                            Duration.between(
                                LocalDate.now().atStartOfDay(),
                                tripWithAttractionsSubList[0].trip.start.atStartOfDay()
                            ).toDays()
                        } Days" else "Current Trips",
                        modifier = Modifier
                            .padding(
                                start = dimensionResource(id = R.dimen.padding_large),
                                end = dimensionResource(id = R.dimen.padding_large),
                                top = dimensionResource(id = R.dimen.padding_medium)
                            )
                    )
                    tripWithAttractionsSubList.forEach { tripWithAttractions ->
                        TripItem(tripWithAttractionsAndLabels = tripWithAttractions,
                            handleEditButtonClick = handleEditButtonClick,
                            onNavigate = onNavigate,
                            modifier = Modifier
                                .padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                                    vertical = dimensionResource(id = R.dimen.padding_small)
                                )
                                .clickable { onItemClick(tripWithAttractions.trip) },
                            curriedOnItemClick = { onItemClick(tripWithAttractions.trip) })
                    }
                }
                itemsIndexed(items = sortedPastTrips) { index, tripWithAttractions ->
                    if (index == 0) Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimensionResource(id = R.dimen.padding_large),
                                end = dimensionResource(id = R.dimen.padding_large),
                                top = dimensionResource(id = R.dimen.padding_medium)
                            )
                            .clickable { pastTripsExpanded = !pastTripsExpanded }) {
                        Text(text = "Past Trips")
                        OutlinedIconButton(onClick = { pastTripsExpanded = !pastTripsExpanded }) {
                            Icon(
                                if (pastTripsExpanded) Icons.Filled.Close else Icons.Filled.Add,
                                contentDescription = null
                            )
                        }
                    }
                    AnimatedVisibility(
                        pastTripsExpanded, enter = expandVertically(
                            expandFrom = Alignment.Top
                        ) + fadeIn(
                            initialAlpha = 0.3f
                        ),
                        exit = slideOutVertically() + shrinkVertically() + fadeOut()
                    ) {
                        TripItem(tripWithAttractionsAndLabels = tripWithAttractions,
                            handleEditButtonClick = handleEditButtonClick,
                            onNavigate = onNavigate,
                            modifier = Modifier
                                .padding(dimensionResource(id = R.dimen.padding_medium))
                                .padding(bottom = if (index == tripAttractionList.size - 1) 72.dp else 0.dp)
                                .clickable { onItemClick(tripWithAttractions.trip) },
                            curriedOnItemClick = { onItemClick(tripWithAttractions.trip) })
                    }
                    if (!pastTripsExpanded && index == sortedPastTrips.size - 1) {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    } else if (sortBy == SortBy.LOCATION) {
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
            val sortedTrips = tripAttractionList.sortedBy {
                haversine(
                    Coordinates(latitude, longitude),
                    Coordinates(it.trip.latitude, it.trip.longitude)
                )
            }
            val groupedTrips = sortedTrips.groupBy {
                haversine(
                    Coordinates(latitude, longitude),
                    Coordinates(it.trip.latitude, it.trip.longitude)
                )
            }
            LazyColumn(
                modifier = modifier
                    .padding(bottom = 82.dp)
            ) {
                itemsIndexed(items = groupedTrips.map { it.value }) { index, tripWithAttractionsSubList ->
                    Text(
                        text = "Distance: ${
                            String.format(
                                "%.1f",
                                groupedTrips.map { it.key }[index]
                            )
                        } km",
                        modifier = Modifier
                            .padding(
                                start = dimensionResource(id = R.dimen.padding_large),
                                end = dimensionResource(id = R.dimen.padding_large),
                                top = dimensionResource(id = R.dimen.padding_medium)
                            )
                    )
                    tripWithAttractionsSubList.forEach { tripWithAttractions ->
                        TripItem(tripWithAttractionsAndLabels = tripWithAttractions,
                            handleEditButtonClick = handleEditButtonClick,
                            onNavigate = onNavigate,
                            modifier = Modifier
                                .padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                                    vertical = dimensionResource(id = R.dimen.padding_small)
                                )
                                .clickable { onItemClick(tripWithAttractions.trip) },
                            curriedOnItemClick = { onItemClick(tripWithAttractions.trip) })
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    text = "Reading satellite data stream...",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Triangulating your location...",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(64.dp))
                Text(
                    text = "(Enable location permissions & GPS)",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall
                )
            }
//=======
//    LazyColumn(
//        modifier = modifier
//    ) {
//        itemsIndexed(items = tripAttractionList) { index, tripWithAttractions ->
//            TripItem(tripWithAttractionsAndLabels = tripWithAttractions,
//                handleEditButtonClick = handleEditButtonClick,
//                onNavigate = onNavigate,
//                modifier = Modifier
//                    .padding(dimensionResource(id = R.dimen.padding_small))
//                    .padding(bottom = if (index == tripAttractionList.size - 1) 72.dp else 0.dp)
//                    .clickable { onItemClick(tripWithAttractions.trip) })
//>>>>>>> ui-improvements
        }
    }
}


@RequiresApi(Build.VERSION_CODES.P)
@Composable
private fun TripItem(
    tripWithAttractionsAndLabels: TripWithAttractionsAndLabels,
    modifier: Modifier = Modifier,
    handleEditButtonClick: (trip: Trip) -> Unit,
    onNavigate: (String) -> Unit,
    curriedOnItemClick: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(5.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation))
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.padding_large),
                    end = dimensionResource(id = R.dimen.padding_large),
                    top = dimensionResource(id = R.dimen.padding_large),
                    bottom = dimensionResource(id = R.dimen.padding_large)
                )
                .fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_extra_small),
                            end = dimensionResource(id = R.dimen.padding_extra_small),
                            top = dimensionResource(id = R.dimen.padding_extra_small),
                            bottom = dimensionResource(id = R.dimen.padding_extra_small),
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (tripWithAttractionsAndLabels.trip.name.isNotEmpty()) tripWithAttractionsAndLabels.trip.name else "Trip ${tripWithAttractionsAndLabels.trip.id}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedIconButton(onClick = {
                            handleEditButtonClick(
                                tripWithAttractionsAndLabels.trip
                            )
                        }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = stringResource(id = R.string.edit_trip)
                            )
                        }
                        var showImage by rememberSaveable { mutableStateOf(false) }
                        if (tripWithAttractionsAndLabels.trip.imageUri.isNotEmpty()) {
                            OutlinedIconButton(
                                onClick = {
                                    showImage = true
                                },
                                modifier = Modifier.padding(
                                    dimensionResource(id = R.dimen.padding_small)
                                )
                            ) {
                                Icon(Icons.Filled.Photo, contentDescription = "View photo")
                            }
                        }

                        if (showImage) {
                            ImageViewer(
                                tripWithAttractionsAndLabels = tripWithAttractionsAndLabels,
                                onDismissRequest = {showImage = false}
                            )
                        }

                        OutlinedIconButton(
                            onClick = {
                                onNavigate("map/${tripWithAttractionsAndLabels.trip.latitude} ${tripWithAttractionsAndLabels.trip.longitude}")
                            },
                            modifier = Modifier.padding(
                                dimensionResource(id = R.dimen.padding_small)
                            )
                        ) {
                            Icon(Icons.Filled.Map, contentDescription = "Open trip on map")
                        }

                    }
                }
                Row(
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_extra_small),
                            end = dimensionResource(id = R.dimen.padding_extra_small),
                            top = dimensionResource(id = R.dimen.padding_extra_small),
                            bottom = dimensionResource(id = R.dimen.padding_extra_small),
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.padding_extra_small),
                            end = dimensionResource(id = R.dimen.padding_extra_small),
                            top = dimensionResource(id = R.dimen.padding_extra_small),
                            bottom = dimensionResource(id = R.dimen.padding_extra_small),
                        ),
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Text(
                            text = "${formatLocalDate(tripWithAttractionsAndLabels.trip.start)} - ${
                                formatLocalDate(
                                    tripWithAttractionsAndLabels.trip.end
                                )
                            }",
                            style = MaterialTheme.typography.titleSmall,
                        )

                        Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_extra_small)))

                        Text(
                            text = "${tripWithAttractionsAndLabels.attractions.filter { it.done }.size}/${tripWithAttractionsAndLabels.attractions.size} attractions visited",
                            style = MaterialTheme.typography.titleSmall,
                        )

                    }
                }
                Row(
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.padding_extra_small),
                            end = dimensionResource(id = R.dimen.padding_extra_small),
                            top = dimensionResource(id = R.dimen.padding_extra_small),
                            bottom = dimensionResource(id = R.dimen.padding_extra_small))
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TripLabels(tripWithAttractionsAndLabels.labels, curriedOnItemClick)
                }
            }

        }
    }
}

@Composable
fun TripLabels(labels: List<Label>, curriedOnItemClick: () -> Unit) {
    if (labels.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.padding_large),
                end = dimensionResource(id = R.dimen.padding_large),
                top = dimensionResource(id = R.dimen.padding_small),
                bottom = dimensionResource(id = R.dimen.padding_small)
            )
        ) {
            itemsIndexed(items = labels) { index, label ->
                AssistChip(
                    onClick = curriedOnItemClick,
                    label = { Text(label.content.text) },
                    modifier = Modifier.padding(start = if (index != 0) dimensionResource(R.dimen.padding_small) else 0.dp)
                )
            }
        }
    }
}
//@Preview(showBackground = true)
//@Composable
//fun HomeBodyPreview() {
//    TripWizardTheme {
//        HomeBody(listOf(
//            Trip(1, "Paris", "Nice Trip"),
//            Trip(2, "Tokyo", "Nice Trip"),
//            Trip(3, "Bora Bora", "Nice Trip")
//        ), onItemClick = {})
//    }
//}

@RequiresApi(Build.VERSION_CODES.P)
fun getImageBitmapFromUri(uri: Uri, context: Context): ImageBitmap {
    val contentResolver: ContentResolver = context.contentResolver
    return ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        .asImageBitmap()

}

//@Preview(showBackground = true)
//@Composable
//fun HomeBodyEmptyListPreview() {
//    TripWizardTheme {
//        HomeBody(listOf(), onItemClick = {})
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun TripItemPreview() {
//    TripWizardTheme {
//        TripItem(
//            Trip(1, "Paris", "Nice Trip"),
//        )
//    }
//}

fun formatLocalDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

}