package com.tripwizard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TurnSharpRight
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tripwizard.ui.navigation.NavigationDestination


@Composable
fun TripWizardBottomNavigationBar(
    currentRoute: NavigationDestination,
    navigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Map,
                    contentDescription = stringResource(NavigationDestination.MAP.navigationLabel!!)
                )
            },
            label = { Text(stringResource(NavigationDestination.MAP.navigationLabel!!)) },
            selected = currentRoute == NavigationDestination.MAP,
            onClick = {
                if (currentRoute != NavigationDestination.MAP) navigate(
                    "map/undefined"
                )
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.TurnSharpRight,
                    contentDescription = stringResource(NavigationDestination.HOME.navigationLabel!!)
                )
            },
            label = { Text(stringResource(NavigationDestination.HOME.navigationLabel!!)) },
            selected = currentRoute == NavigationDestination.HOME,
            onClick = {
                if (currentRoute != NavigationDestination.HOME) navigate(
                    NavigationDestination.HOME.route
                )
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = stringResource(NavigationDestination.DISCOVER.navigationLabel!!)
                )
            },
            label = { Text(stringResource(NavigationDestination.DISCOVER.navigationLabel!!)) },
            selected = currentRoute == NavigationDestination.DISCOVER,
            onClick = {
                if (currentRoute != NavigationDestination.DISCOVER) navigate(
                    NavigationDestination.DISCOVER.route
                )
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(NavigationDestination.SETTINGS.navigationLabel!!)
                )
            },
            label = { Text(stringResource(NavigationDestination.SETTINGS.navigationLabel!!)) },
            selected = currentRoute == NavigationDestination.SETTINGS,
            onClick = {
                if (currentRoute != NavigationDestination.SETTINGS) navigate(
                    NavigationDestination.SETTINGS.route
                )
            }
        )
    }
}