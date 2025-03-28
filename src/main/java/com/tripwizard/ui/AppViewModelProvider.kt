package com.tripwizard.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tripwizard.TripWizardApplication
import com.tripwizard.ui.discover.DiscoverViewModel
import com.tripwizard.ui.map.MapViewModel
import com.tripwizard.ui.home.HomeViewModel
import com.tripwizard.ui.tripdetails.TripDetailsViewModel
import com.tripwizard.ui.usersettings.UserSettingsViewModel


object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MapViewModel(
                this.createSavedStateHandle(),
                tripWizardApplication().container.tripsRepository,
                tripWizardApplication().container.userSettingsRepository,
                tripWizardApplication().container.notificationRepository
            )
        }

        initializer {
            HomeViewModel(
                tripWizardApplication().container.tripsRepository,
                tripWizardApplication().container.attractionRepository,
                tripWizardApplication().container.labelRepository,
                tripWizardApplication().container.userSettingsRepository,
                tripWizardApplication().container.notificationRepository
            )
        }

        initializer {
            UserSettingsViewModel(
                tripWizardApplication().container.tripsRepository,
                tripWizardApplication().container.userSettingsRepository,
                tripWizardApplication().container.notificationRepository
            )
        }

        initializer {
            DiscoverViewModel(
                tripWizardApplication().container.tripsRepository,
                tripWizardApplication().container.userSettingsRepository,
                tripWizardApplication().container.notificationRepository
            )
        }

        initializer {
            TripDetailsViewModel(
                this.createSavedStateHandle(),
                tripWizardApplication().container.tripsRepository,
                tripWizardApplication().container.attractionRepository,
                tripWizardApplication().container.userSettingsRepository,
                tripWizardApplication().container.notificationRepository
            )
        }
    }
}

fun CreationExtras.tripWizardApplication(): TripWizardApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TripWizardApplication)