package com.tripwizard

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tripwizard.ui.navigation.TripWizardNavHost

@Composable
fun TripWizardApp(
    isDarkMode: Boolean,
    setIsDarkMode: (Boolean?) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    TripWizardNavHost(
        isDarkMode = isDarkMode,
        setIsDarkMode = setIsDarkMode,
        navController = navController
    )
}