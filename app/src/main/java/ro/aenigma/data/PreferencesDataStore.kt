package ro.aenigma.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.util.dataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val ALLOW_NOTIFICATIONS_PREFERENCE = "notifications-permission"
        private const val TOR_PREFERENCE = "use-tor"
        private const val ORBOT_PREFERENCE = "use-orbot"
        private const val NAME_PREFERENCE = "name"
        private const val ENCRYPTED_DATABASE_PASSPHRASE_PREFERENCE = "encrypted-database-passphrase"
        private const val DATABASE_PASSPHRASE_SIZE_BYTES = 128
        private const val NEWS_FEED_URI = "news-feed-file"
        private const val NOTIFICATION_SERVICE_PREFERENCE = "use-notification-service"
    }

    private object PreferenceKeys {
        val notificationsAllowed = booleanPreferencesKey(ALLOW_NOTIFICATIONS_PREFERENCE)
        val name = stringPreferencesKey(NAME_PREFERENCE)
        val useTor = booleanPreferencesKey(TOR_PREFERENCE)
        val useOrbot = booleanPreferencesKey(ORBOT_PREFERENCE)
        val encryptedDatabasePassphrase =
            byteArrayPreferencesKey(ENCRYPTED_DATABASE_PASSPHRASE_PREFERENCE)
        val newsFeedUri = stringPreferencesKey(NEWS_FEED_URI)
        val notificationServicePreference = booleanPreferencesKey(NOTIFICATION_SERVICE_PREFERENCE)
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

    suspend fun saveOrbotPreference(useOrbot: Boolean): Boolean {
        return savePreference(useOrbot, PreferenceKeys.useOrbot)
    }

    suspend fun saveEncryptedDatabasePassphrase() {
        val key = CryptoProvider.generateRandomBytes(DATABASE_PASSPHRASE_SIZE_BYTES)
        val encryptedKey = CryptoProvider.masterKeyEncrypt(key)
        key.fill(0)
        if (encryptedKey != null) {
            savePreference(encryptedKey, PreferenceKeys.encryptedDatabasePassphrase)
            encryptedKey.fill(0)
        }
    }

    suspend fun saveNewsFeedUri(uri: Uri): Boolean {
        return savePreference(uri.toString(), PreferenceKeys.newsFeedUri)
    }

    suspend fun saveNotificationServicePreference(notificationServicePreference: Boolean): Boolean {
        return savePreference(
            notificationServicePreference,
            PreferenceKeys.notificationServicePreference
        )
    }

    private fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[key] ?: defaultValue }
    }

    val notificationsAllowed: Flow<Boolean> =
        getPreference(PreferenceKeys.notificationsAllowed, true)

    val name: Flow<String> = getPreference(PreferenceKeys.name, "")

    val useTor: Flow<Boolean> = getPreference(PreferenceKeys.useTor, false)

    val useOrbot: Flow<Boolean> = getPreference(PreferenceKeys.useOrbot, false)

    val encryptedDatabasePassphrase: Flow<ByteArray> =
        getPreference(PreferenceKeys.encryptedDatabasePassphrase, byteArrayOf())

    val newsFeedUri: Flow<Uri?> = getPreference(PreferenceKeys.newsFeedUri, "")
        .map { uri -> uri.takeIf { value -> value.isNotBlank() }?.toUri() }

    val notificationServicePreference: Flow<Boolean> =
        getPreference(PreferenceKeys.notificationServicePreference, true)
}
