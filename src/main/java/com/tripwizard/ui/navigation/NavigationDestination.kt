package com.tripwizard.ui.navigation

import androidx.annotation.StringRes
import com.tripwizard.R

enum class NavigationDestination(
    @StringRes val titleRes: Int,
    @StringRes val navigationLabel: Int? = null,
    val route: String
) {
    HOME(
        titleRes = R.string.app_name,
        navigationLabel = R.string.home_navigation_label,
        route = "home"
    ),
    MAP(
        titleRes = R.string.map,
        navigationLabel = R.string.map_navigation_label,
        route = "map/{initialCoordinates}"
    ),
    DISCOVER(
        titleRes = R.string.discover,
        navigationLabel = R.string.discover_navigation_label,
        route = "discover"
    ),
    SETTINGS(
        titleRes = R.string.settings,
        navigationLabel = R.string.settings_navigation_label,
        route = "settings"
    ),
    TRIP_DETAILS(titleRes = R.string.trip_details, route = "trip_details/{tripId}")
}