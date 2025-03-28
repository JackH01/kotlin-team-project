package com.tripwizard.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tripwizard.R
import com.tripwizard.data.LabelOptions
import com.tripwizard.data.Trip
import com.tripwizard.ui.shared.Coordinates

@Composable
fun LocationSelector(
    locationSelectedCallback: (Coordinates, radius: Float) -> Unit,
    initialLocationSet: Boolean = true,
    initialLatitude: Double,
    initialLongitude: Double,
    initialRadius: Float,
    locationPermissionsEnabled: Boolean,
    onDismissRequest: () -> Unit
) {
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

    BackHandler {
        onDismissRequest()
    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 64.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.dialog_elevation))
        ) {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Location Selector") },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button)
                            )
                        }
                    }
                )
                var radius by rememberSaveable { mutableFloatStateOf(initialRadius) }
                GoogleMap(
                    modifier = Modifier.weight(1f),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = locationPermissionsEnabled)
                ) {
                    Marker(
                        state = MarkerState(position = cameraPositionState.position.target),
                        title = "Trip Location",
                        snippet = "Trip Location"
                    )
                    Circle(
                        center = cameraPositionState.position.target,
                        radius = radius.toDouble()
                    )
                }
                Column(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))) {
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 2000f..30000f,
                    )
                    Text(text = "Radius: ${String.format("%.1f", (radius / 1000))} km")
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.padding_large),
                        )
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        locationSelectedCallback(
                            Coordinates(
                                cameraPositionState.position.target.latitude,
                                cameraPositionState.position.target.longitude
                            ),
                            radius
                        )
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}