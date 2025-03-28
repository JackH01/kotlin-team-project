package com.tripwizard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val USER_SETTINGS_DATASTORE_NAME = "user_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_SETTINGS_DATASTORE_NAME
)

interface AppContainer {
    val tripsRepository: TripsRepository
    val userSettingsRepository: UserSettingsRepository
    val attractionRepository: AttractionRepository
    val labelRepository: LabelRepository
    val notificationRepository: NotificationRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val tripsRepository: TripsRepository by lazy {
        OfflineTripsRepository(TripWizardDatabase.getDatabase(context).tripDao())
    }

    override val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(context.dataStore)
    }

    override val attractionRepository: AttractionRepository by lazy {
        OfflineAttractionRepository(TripWizardDatabase.getDatabase(context).attractionDao())
    }

    override val labelRepository: LabelRepository by lazy {
        OfflineLabelRepository(TripWizardDatabase.getDatabase(context).labelDao())
    }

    override val notificationRepository: NotificationRepository by lazy {
        OfflineNotificationRepository(TripWizardDatabase.getDatabase(context).notificationDao())
    }
}