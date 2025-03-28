package com.tripwizard.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripwizard.R
import com.tripwizard.data.Label
import com.tripwizard.data.TripWithAttractionsAndLabels
import com.tripwizard.data.templateTrips
import com.tripwizard.ui.AppViewModelProvider
import com.tripwizard.ui.components.TripWizardBottomNavigationBar
import com.tripwizard.ui.components.TripWizardTopAppBar
import com.tripwizard.ui.home.HomeViewModel
import com.tripwizard.ui.navigation.NavigationDestination
import com.tripwizard.ui.tripdetails.TripDetailsViewModel
import com.tripwizard.ui.usersettings.UserSettingsViewModel
import com.tripwizard.ui.utils.handleNotifications

@Composable
fun DiscoverScreen(
    onNavigate: (String) -> Unit,
    navigateUp: () -> Unit,
    navigateToTripDetailsView: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val discoverUiState by viewModel.discoverUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val addNewTripWithLabelsAndAttractions = viewModel::addNewTripWithLabelsAndAttractions

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    handleNotifications(
        scope = scope,
        snackbarHostState = snackbarHostState,
        notificationsUiState = viewModel.notificationsUiState,
        updateNotification = viewModel::updateNotification,
        insertNotifications = viewModel::insertNotifications,
        tripsWithAttractionsAndLabelsList = discoverUiState.tripWithAttractionsAndLabelsList
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TripWizardTopAppBar(
                title = stringResource(NavigationDestination.DISCOVER.titleRes!!),
                canNavigateBack = true,
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
                notificationsUiState = viewModel.notificationsUiState,
                tripsWithAttractionsAndLabelsList = discoverUiState.tripWithAttractionsAndLabelsList,
                updateNotification = viewModel::updateNotification,
            )
        },
        bottomBar = {
            TripWizardBottomNavigationBar(
                currentRoute = NavigationDestination.DISCOVER,
                navigate = onNavigate
            )
        }) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(dimensionResource(id = R.dimen.padding_medium))
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
        ) {
            DiscoverHeader()
            DiscoverBody(
                onItemClick = navigateToTripDetailsView,
                addNewTripWithLabelsAndAttractions = addNewTripWithLabelsAndAttractions,
                navigateToTripDetailsView = navigateToTripDetailsView
            )
        }
    }
}

@Composable
fun DiscoverHeader() {
    Text(
        "If you're looking for your next destination and " +
                "need a little hand getting started, look no further!"
    )
}

@Composable
fun DiscoverBody(
    onItemClick: (Int) -> Unit,
    addNewTripWithLabelsAndAttractions: (TripWithAttractionsAndLabels) -> Unit,
    navigateToTripDetailsView: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        LazyColumn {
            items(items = templateTrips, key = { it.trip.id }) { templateTrip ->
                TemplateTripItem(
                    tripWithAttractionsAndLabels = templateTrip,
                    addNewTripWithLabelsAndAttractions = addNewTripWithLabelsAndAttractions,
                    navigateToTripDetailsView = navigateToTripDetailsView,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_medium))
                        .clickable { onItemClick(templateTrip.trip.id) })
            }
        }
    }
}


@Composable
private fun TemplateTripItem(
    tripWithAttractionsAndLabels: TripWithAttractionsAndLabels,
    modifier: Modifier = Modifier,
    addNewTripWithLabelsAndAttractions: (TripWithAttractionsAndLabels) -> Unit,
    navigateToTripDetailsView: (Int) -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation))
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.padding_large),
                end = dimensionResource(id = R.dimen.padding_large),
                top = dimensionResource(id = R.dimen.padding_large),
                bottom = if (tripWithAttractionsAndLabels.labels.isNotEmpty()) 0.dp else dimensionResource(
                    id = R.dimen.padding_large
                )
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = if (tripWithAttractionsAndLabels.trip.name.isNotEmpty()) tripWithAttractionsAndLabels.trip.name else "Trip ${tripWithAttractionsAndLabels.trip.id}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_extra_small)))
                Text(
                    text = "${tripWithAttractionsAndLabels.attractions.size} Suggested Attractions",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(dimensionResource(id = R.dimen.padding_extra_small)))
                for (attraction in tripWithAttractionsAndLabels.attractions) {
                    Text(attraction.name)
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            OutlinedIconButton(onClick = {
                addNewTripWithLabelsAndAttractions(tripWithAttractionsAndLabels)
                navigateToTripDetailsView(tripWithAttractionsAndLabels.trip.id)
            }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.edit_trip)
                )
            }
        }
        TemplateTripLabels(tripWithAttractionsAndLabels.labels)
    }
}

@Composable
fun TemplateTripLabels(labels: List<Label>) {
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
                    onClick = {},
                    label = { Text(label.content.text) },
                    modifier = Modifier.padding(start = if (index != 0) dimensionResource(R.dimen.padding_small) else 0.dp)
                )
            }
        }
    }
}

