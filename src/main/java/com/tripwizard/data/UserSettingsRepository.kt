package com.tripwizard.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tripwizard.ui.shared.Coordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


class UserSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val USERNAME = stringPreferencesKey("username")
        val DARK_MODE_PREFERRED = booleanPreferencesKey("dark_mode_preferred")
        val LATEST_USER_LOCATION = stringPreferencesKey("latest_user_location")
        const val TAG = "UserSettingsRepo"
    }

    val username: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading user settings.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { it[USERNAME] ?: "" }

    suspend fun saveUsername(username: String) {
        dataStore.edit { settings ->
            settings[USERNAME] = username
        }
    }

    val darkModePreferred: Flow<Boolean?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading user settings.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { settings ->
            settings[DARK_MODE_PREFERRED]
        }

    suspend fun saveDarkModePreferred(isDarkMode: Boolean) {
        dataStore.edit { settings ->
            settings[DARK_MODE_PREFERRED] = isDarkMode
        }
    }

    val latestUserLocation: Flow<Coordinates?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading user settings.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            val databaseValue = it[LATEST_USER_LOCATION]
            var returnValue: Coordinates? = null
            if (databaseValue != null) {
                val parsedCoordinates = databaseValue.split(" ")
                returnValue =
                    Coordinates(parsedCoordinates[0].toDouble(), parsedCoordinates[1].toDouble())
            }
            returnValue
        }

    suspend fun saveLatestUserLocation(coordinates: Coordinates) {
        dataStore.edit { settings ->
            val location = "${coordinates.latitude} ${coordinates.longitude}"
            settings[LATEST_USER_LOCATION] = location
        }
    }
}