package com.example.enigma.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.enigma.util.Constants
import com.example.enigma.util.Constants.Companion.ALLOW_NOTIFICATIONS_PREFERENCE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_PREFERENCES)

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferenceKeys {
        val notificationsAllowed = booleanPreferencesKey(ALLOW_NOTIFICATIONS_PREFERENCE)
    }

    private val dataStore = context.dataStore

    suspend fun saveNotificationsAllowed(allowed: Boolean)
    {
        try {
            dataStore.edit { preference ->
                preference[PreferenceKeys.notificationsAllowed] = allowed
            }
        }
        catch (_: Exception)
        {
            // TODO: do something with the exception, if required
        }
    }

    val notificationsAllowed: Flow<Boolean> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map {
            preferences -> preferences[PreferenceKeys.notificationsAllowed] ?: true
        }
}