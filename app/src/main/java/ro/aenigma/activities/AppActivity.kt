/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.activities

import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ro.aenigma.data.PreferencesDataStore
import ro.aenigma.di.DbPassphraseKeeper
import ro.aenigma.services.Notifier
import ro.aenigma.services.SignalrController
import ro.aenigma.services.OnionRoutingServiceController
import ro.aenigma.ui.biometric.SecuredApp
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.navigation.SetupNavigation
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.util.Constants.Companion.APP_DOMAIN
import ro.aenigma.util.Constants.Companion.ARTICLES_DOMAIN
import ro.aenigma.util.Constants.Companion.WEB_DOMAIN
import ro.aenigma.viewmodels.MainViewModel
import javax.inject.Inject
import androidx.work.WorkManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import ro.aenigma.AenigmaApp
import ro.aenigma.R
import ro.aenigma.data.LocalDataSource
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.services.NotificationServiceController
import ro.aenigma.services.OnionRoutingServiceMonitor
import ro.aenigma.ui.screens.common.CheckNotificationsPermission
import ro.aenigma.ui.screens.common.NotificationsPermissionRequiredDialog
import ro.aenigma.ui.screens.contacts.SetupUserNameDialog
import ro.aenigma.util.Constants
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_MAX_COUNT
import ro.aenigma.util.Constants.Companion.ATTACHMENT_MAX_SIZE
import ro.aenigma.util.Constants.Companion.AUTHENTICATION_DEADLINE
import ro.aenigma.util.ContextExtensions.filterSharedUris
import ro.aenigma.util.ContextExtensions.openApplicationDetails
import ro.aenigma.util.ContextExtensions.openInBrowser
import ro.aenigma.util.LongExtensions.toMegabytes
import ro.aenigma.util.StringExtensions.isTextMime
import ro.aenigma.util.UriExtensions.getArticleUri
import ro.aenigma.util.UriExtensions.isSharedData
import ro.aenigma.workers.extensions.WorkManagerExtensions.schedulePeriodicClientSync

@AndroidEntryPoint
class AppActivity : FragmentActivity() {

    @Inject
    lateinit var onionRoutingServiceController: OnionRoutingServiceController

    @Inject
    lateinit var onionRoutingServiceMonitor: OnionRoutingServiceMonitor

    @Inject
    lateinit var signalrController: SignalrController

    @Inject
    lateinit var notificationServiceController: NotificationServiceController

    @Inject
    lateinit var notifier: Notifier

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    @Inject
    lateinit var localDataSource: LocalDataSource

    @Inject
    lateinit var workManager: WorkManager

    private val dbPassphraseLoaded = MutableStateFlow(false)

    private val isAuthenticated = MutableStateFlow(true)

    private val isAuthError = MutableStateFlow(false)

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var navHostController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        loadDbPassphrase()
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        setContent {
            ApplicationComposeTheme {
                val authenticated by isAuthenticated.collectAsState()
                val authError by isAuthError.collectAsState()
                val passphraseLoaded by dbPassphraseLoaded.collectAsState()
                val userName by mainViewModel.userName.collectAsState()
                val notificationsAllowed by mainViewModel.notificationsAllowed.collectAsState()
                val context = LocalContext.current
                var notificationPermissionDialogVisible by remember { mutableStateOf(false) }

                navHostController = rememberNavController()
                SecuredApp(
                    isDeviceSecured = keyguardManager.isDeviceSecure,
                    isAuthenticated = authenticated,
                    isAuthError = authError,
                    dbPassphraseLoaded = passphraseLoaded,
                    onAuthSuccess = {
                        isAuthenticated.value = true
                        saveAuthenticationTimestamp()
                    },
                    onAuthFailed = { isAuthError.value = true }
                ) {
                    LaunchedEffect(key1 = true) {
                        observeTorPreference()
                        observeTorProxy()
                        observeClientConnectivity()
                        observeNotificationServicePreference()
                        createBroadcastContact()
                        handleAppLink()
                        handleSharedFiles()
                        schedulePeriodicSync()
                    }

                    SetupUserNameDialog(
                        visible = userName.isBlank(),
                        onConfirmClicked = { userName -> mainViewModel.setupName(userName) }
                    )

                    CheckNotificationsPermission(
                        onPermissionGranted = { granted ->
                            notificationPermissionDialogVisible = !granted && notificationsAllowed
                            if (granted) {
                                mainViewModel.saveNotificationsPreference(true)
                            }
                        }
                    )

                    NotificationsPermissionRequiredDialog(
                        visible = notificationPermissionDialogVisible,
                        onPositiveButtonClicked = {
                            notificationPermissionDialogVisible = false
                            context.openApplicationDetails()
                        },
                        onNegativeButtonClicked = { rememberDecision ->
                            if (rememberDecision) {
                                mainViewModel.saveNotificationsPreference(false)
                            }
                            notificationPermissionDialogVisible = false
                        }
                    )

                    SetupNavigation(
                        navHostController = navHostController,
                        notifier = notifier,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }

    private fun loadDbPassphrase() {
        lifecycleScope.launch(Dispatchers.IO) {
            preferencesDataStore.encryptedDatabasePassphrase.collect { data ->
                if (data.isEmpty()) {
                    preferencesDataStore.saveEncryptedDatabasePassphrase()
                } else {
                    DbPassphraseKeeper.dbPassphrase.value = data
                    dbPassphraseLoaded.value = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notifier.enterForeground()
        resetAuthentication()
        resetClient()
    }

    override fun onPause() {
        super.onPause()
        notifier.enterBackground()
        saveAuthenticationTimestamp()
    }

    private fun saveAuthenticationTimestamp() {
        (application as AenigmaApp).applicationScope.launch {
            localDataSource.saveAuthenticationTimestamp()
        }
    }

    private fun resetAuthentication() {
        lifecycleScope.launch(Dispatchers.IO) {
            val authenticationTimestamp =
                localDataSource.authenticationTimestamp.firstOrNull() ?: 0L
            val elapsed = if (authenticationTimestamp > 0) {
                System.currentTimeMillis() - authenticationTimestamp
            } else {
                AUTHENTICATION_DEADLINE + 1
            }
            if (elapsed > AUTHENTICATION_DEADLINE) {
                isAuthenticated.value = false
                isAuthError.value = false
            }
        }
        isAuthError.value = false
    }

    private fun resetClient() {
        if (dbPassphraseLoaded.value) {
            lifecycleScope.launch(Dispatchers.IO) { signalrController.resetClient() }
        }
    }

    private fun schedulePeriodicSync() {
        workManager.schedulePeriodicClientSync()
    }

    private fun observeTorPreference() {
        return onionRoutingServiceController.observeTorPreferences(this)
    }

    private fun observeTorProxy() {
        return onionRoutingServiceMonitor.observeSocksProxy(this)
    }

    private fun observeClientConnectivity() {
        return signalrController.observeSignalrConnection(this)
    }

    private fun observeNotificationServicePreference() {
        return notificationServiceController.observeNotificationServicePreference(this)
    }

    private fun createBroadcastContact() {
        lifecycleScope.launch(Dispatchers.IO) {
            val broadcastContact = ContactDtoFactory.createContact(
                address = Constants.BROADCAST_CONTACT_ADDRESS,
                name = applicationContext.getString(R.string.broadcast),
                publicKey = null,
                guardAddress = null,
                guardHostname = null
            )
            localDataSource.insertOrIgnoreContact(broadcastContact)
        }
    }

    private fun handleAppLink() {
        val appLinkData = intent.data ?: return
        val domain = appLinkData.host?.lowercase() ?: return
        if (intent.action != Intent.ACTION_VIEW) {
            return
        }
        when (domain) {
            APP_DOMAIN -> handleAppDomain(appLinkData)
            ARTICLES_DOMAIN -> handleArticlesDomain(appLinkData)
            WEB_DOMAIN -> handleWebDomain(appLinkData)
        }
    }

    private fun handleSharedFiles() {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type.isTextMime()) {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                        mainViewModel.setText(text)
                    }
                } else {
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    }
                    uri?.let { onSingleFileReceived(it) }
                }
                Screens(navController = navHostController, mainViewModel = mainViewModel).root()
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                }
                uris?.let { onMultipleFilesReceived(it) }
                Screens(navController = navHostController, mainViewModel = mainViewModel).root()
            }
        }
    }

    private fun onSingleFileReceived(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val uriFilterResult = applicationContext.filterSharedUris(uris = listOf(uri))
            mainViewModel.setAttachments(uriFilterResult.acceptedUris.map { it.toString() })
            if (uriFilterResult.tooLargeCount > 0) {
                val fileTooLargeString = applicationContext.getString(
                    R.string.files_too_large,
                    ATTACHMENT_MAX_SIZE.toMegabytes()
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, fileTooLargeString, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onMultipleFilesReceived(uris: List<Uri>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val uriFilterResult = applicationContext.filterSharedUris(uris = uris)
            mainViewModel.setAttachments(uriFilterResult.acceptedUris.map { it.toString() })
            if (uriFilterResult.tooLargeCount > 0) {
                val fileTooLargeString = applicationContext.getString(
                    R.string.files_too_large,
                    ATTACHMENT_MAX_SIZE.toMegabytes()
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, fileTooLargeString, Toast.LENGTH_LONG).show()
                }
            }
            if (uriFilterResult.excessCount > 0) {
                val tooManyAttachmentsString = getString(
                    R.string.attachment_files_limit,
                    ATTACHMENTS_MAX_COUNT
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, tooManyAttachmentsString, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun handleAppDomain(uri: Uri) {
        if (uri.isSharedData()) {
            Screens(
                navController = navHostController,
                mainViewModel = mainViewModel
            ).getSharedContact(uri.toString())
        } else {
            applicationContext.openInBrowser(uri)
        }
    }

    private fun handleArticlesDomain(uri: Uri) {
        if (uri.host == ARTICLES_DOMAIN) {
            Screens(
                navController = navHostController,
                mainViewModel = mainViewModel
            ).article(uri.toString(), null, null)
        } else {
            applicationContext.openInBrowser(uri)
        }
    }

    private fun handleWebDomain(uri: Uri) {
        val articleUri = uri.getArticleUri()
        if (articleUri != null) {
            handleArticlesDomain(articleUri)
        } else {
            applicationContext.openInBrowser(uri)
        }
    }
}
