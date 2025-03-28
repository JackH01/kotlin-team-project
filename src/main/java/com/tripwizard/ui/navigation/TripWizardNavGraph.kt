package com.tripwizard.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tripwizard.ui.discover.DiscoverScreen
import com.tripwizard.ui.home.HomeScreen
import com.tripwizard.ui.map.MapScreen
import com.tripwizard.ui.tripdetails.TripDetailsScreen
import com.tripwizard.ui.usersettings.UserSettingsScreen
import com.tripwizard.ui.utils.darkModePreferredListener

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun TripWizardNavHost(
    isDarkMode: Boolean,
    setIsDarkMode: (Boolean?) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val curried = darkModePreferredListener(isDarkMode, setIsDarkMode)
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.HOME.name,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        modifier = modifier
    ) {
        composable(
            route = NavigationDestination.MAP.route,
            arguments = listOf(navArgument("initialCoordinates") {
                type = NavType.StringType
            }),
        ) {
            MapScreen(
                darkModePreferredListener = curried,
                onNavigate = { route -> navController.navigate(route) },
                navigateUp = { navController.navigateUp() }
            )
        }
        composable(route = NavigationDestination.HOME.route,
        ) {
            HomeScreen(
                darkModePreferredListener = curried,
                onNavigate = { route -> navController.navigate(route) },
                navigateToTripDetailsView = {
                    navController.navigate("trip_details/${it}")
                }
            )
        }
        composable(route = NavigationDestination.DISCOVER.route,
//            enterTransition = {
//                scaleIn(transformOrigin = TransformOrigin(0.5f, 0.5f)) +
//                        fadeIn() + expandIn(expandFrom = Alignment.TopStart)
//            },
//            exitTransition = {
//                scaleOut(transformOrigin = TransformOrigin(0.5f, 0.5f)) +
//                        fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
//            }) {
        ){
            DiscoverScreen(
                onNavigate = { route -> navController.navigate(route) },
                navigateUp = { navController.navigateUp() },
                navigateToTripDetailsView = {
                    navController.navigate("trip_details/${it}")
                }
            )
        }
        composable(route = NavigationDestination.SETTINGS.route,
//            enterTransition = {
//                scaleIn(transformOrigin = TransformOrigin(0.5f, 0.5f)) +
//                        fadeIn() + expandIn(expandFrom = Alignment.TopStart)
//            },
//            exitTransition = {
//                scaleOut(transformOrigin = TransformOrigin(0.5f, 0.5f)) +
//                        fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
//            }
//
            ) {
            UserSettingsScreen(
                darkModePreferredListener = curried,
                isDarkMode = isDarkMode,
                onNavigate = { route -> navController.navigate(route) },
                navigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = NavigationDestination.TRIP_DETAILS.route,
            arguments = listOf(navArgument("tripId") {
                type = NavType.IntType
            }),
//            enterTransition = {
//                scaleIn(transformOrigin = TransformOrigin(0.5f, 0.5f)) +
//                        fadeIn() + expandIn(expandFrom = Alignment.TopStart)
//            },
//            exitTransition = {
//                scaleOut(transformOrigin = TransformOrigin(0.5f, 0.5f)) +
//                        fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
//            }
        ) {
            TripDetailsScreen(
                darkModePreferredListener = curried,
                onNavigate = { route -> navController.navigate(route) },
                navigateUp = { navController.navigateUp() },
                navigateToHome = {
                    navController.popBackStack(
                        route = NavigationDestination.HOME.route,
                        inclusive = false
                    )
                }
            )
        }
    }
}