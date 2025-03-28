package com.tripwizard

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tripwizard.ui.theme.TripWizardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val defaultIsDarkMode = isSystemInDarkTheme()
            var isDarkMode by rememberSaveable { mutableStateOf(defaultIsDarkMode) }
            val setIsDarkMode: (Boolean?) -> Unit = { isDarkMode = it ?: defaultIsDarkMode }
            TripWizardTheme(darkTheme = isDarkMode) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TripWizardApp(isDarkMode = isDarkMode, setIsDarkMode = setIsDarkMode)
                }
            }
        }
    }
}
