package ro.aenigma.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ro.aenigma.util.Constants
import ro.aenigma.util.Constants.Companion.ALLOW_NOTIFICATIONS_PREFERENCE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ro.aenigma.util.Constants.Companion.NAME_PREFERENCE
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_PREFERENCES)

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferenceKeys {
        val notificationsAllowed = booleanPreferencesKey(ALLOW_NOTIFICATIONS_PREFERENCE)
        val name = stringPreferencesKey(NAME_PREFERENCE)
    }

    private val dataStore = context.dataStore

    private suspend fun <T> savePreference(data: T, key: Preferences.Key<T>) {
        try {
            dataStore.edit { preference ->
                preference[key] = data
            }
        } catch (_: Exception) {
            // TODO: do something with the exception, if required
        }
    }

    suspend fun saveNotificationsAllowed(allowed: Boolean) {
        return savePreference(allowed, PreferenceKeys.notificationsAllowed)
    }

    suspend fun saveName(name: String) {
        return savePreference(name, PreferenceKeys.name)
    }

    private fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    val notificationsAllowed: Flow<Boolean> =
        getPreference(PreferenceKeys.notificationsAllowed, true)

    val name: Flow<String> = getPreference(PreferenceKeys.name, "")
}