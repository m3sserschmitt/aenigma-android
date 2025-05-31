package ro.aenigma.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ro.aenigma.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ro.aenigma.crypto.CryptoProvider
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_PREFERENCES)

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ALLOW_NOTIFICATIONS_PREFERENCE = "notifications-permission"
        private const val TOR_PREFERENCE = "use-tor"
        private const val NAME_PREFERENCE = "name"
        private const val ENCRYPTED_DATABASE_PASSPHRASE_PREFERENCE = "encrypted-database-passphrase"
        private const val DATABASE_PASSPHRASE_SIZE_BYTES = 128
    }

    private object PreferenceKeys {
        val notificationsAllowed = booleanPreferencesKey(ALLOW_NOTIFICATIONS_PREFERENCE)
        val name = stringPreferencesKey(NAME_PREFERENCE)
        val useTor = booleanPreferencesKey(TOR_PREFERENCE)
        val encryptedDatabasePassphrase =
            byteArrayPreferencesKey(ENCRYPTED_DATABASE_PASSPHRASE_PREFERENCE)
    }

    private val dataStore = context.dataStore

    private suspend fun <T> savePreference(data: T, key: Preferences.Key<T>): Boolean {
        try {
            dataStore.edit { preference ->
                preference[key] = data
            }
        } catch (_: Exception) {
            return false
        }
        return true
    }

    suspend fun saveNotificationsAllowed(allowed: Boolean): Boolean {
        return savePreference(allowed, PreferenceKeys.notificationsAllowed)
    }

    suspend fun saveName(name: String): Boolean {
        return savePreference(name, PreferenceKeys.name)
    }

    suspend fun saveTorPreference(useTor: Boolean): Boolean {
        return savePreference(useTor, PreferenceKeys.useTor)
    }

    suspend fun saveEncryptedDatabasePassphrase() {
        val key = CryptoProvider.generateRandomBytes(DATABASE_PASSPHRASE_SIZE_BYTES)
        val encryptedKey = CryptoProvider.masterKeyEncrypt(key)
        if (encryptedKey != null) {
            savePreference(encryptedKey, PreferenceKeys.encryptedDatabasePassphrase)
        }
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

    val useTor: Flow<Boolean> = getPreference(PreferenceKeys.useTor, false)

    val encryptedDatabasePassphrase: Flow<ByteArray> =
        getPreference(PreferenceKeys.encryptedDatabasePassphrase, byteArrayOf())
}
