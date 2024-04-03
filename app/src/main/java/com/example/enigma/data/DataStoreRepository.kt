package com.example.enigma.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.enigma.util.Constants.Companion.DATA_STORE_PREFERENCE_NAME
import com.example.enigma.util.Constants.Companion.GRAPH_VERSION_PREFERENCES_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
by preferencesDataStore(DATA_STORE_PREFERENCE_NAME)

@Singleton
class DataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferenceKeys {
        val graphVersionKey = stringPreferencesKey(GRAPH_VERSION_PREFERENCES_KEY)
    }

    private val dataStore = context.dataStore

    suspend fun saveGraphVersion(version: String)
    {
        dataStore.edit { preference ->
            preference[PreferenceKeys.graphVersionKey] = version
        }
    }

    suspend fun getGraphVersion() = dataStore.data.catch { exception ->
        if(exception is IOException)
        {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[PreferenceKeys.graphVersionKey] ?: ""
    }
}
