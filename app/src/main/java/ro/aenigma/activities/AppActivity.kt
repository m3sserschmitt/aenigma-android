package ro.aenigma.activities

import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import ro.aenigma.R
import ro.aenigma.data.LocalDataSource
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.services.NotificationServiceController
import ro.aenigma.services.OnionRoutingServiceMonitor
import ro.aenigma.ui.screens.common.CheckNotificationsPermission
import ro.aenigma.ui.screens.common.NotificationsPermissionRequiredDialog
import ro.aenigma.ui.screens.contacts.SetupUserNameDialog
import ro.aenigma.util.Constants
import ro.aenigma.util.Constants.Companion.AUTHENTICATION_DEADLINE
import ro.aenigma.util.ContextExtensions.openApplicationDetails
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

    private val isAuthenticated = MutableStateFlow(false)

    private val isAuthError = MutableStateFlow(false)

    private val lastPausedTime = MutableStateFlow(0L)

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
                val auth by isAuthenticated.collectAsState()
                val authError by isAuthError.collectAsState()
                val passphraseLoaded by dbPassphraseLoaded.collectAsState()
                val userName by mainViewModel.userName.collectAsState()
                val notificationsAllowed by mainViewModel.notificationsAllowed.collectAsState()
                val context = LocalContext.current
                var notificationPermissionDialogVisible by remember { mutableStateOf(false) }

                navHostController = rememberNavController()
                SecuredApp(
                    isDeviceSecured = keyguardManager.isDeviceSecure,
                    isAuthenticated = auth,
                    isAuthError = authError,
                    dbPassphraseLoaded = passphraseLoaded,
                    onAuthSuccess = { isAuthenticated.value = true },
                    onAuthFailed = { isAuthError.value = true }
                ) {
                    LaunchedEffect(key1 = true) {
                        observeTorPreference()
                        observeTorProxy()
                        observeClientConnectivity()
                        observeNotificationServicePreference()
                        createBroadcastContact()
                        handleAppLink()
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
        if (dbPassphraseLoaded.value) {
            resetClient()
        }
    }

    override fun onPause() {
        super.onPause()
        notifier.enterBackground()
        lastPausedTime.value = System.currentTimeMillis()
    }

    private fun resetAuthentication() {
        val lastPausedTimeValue = lastPausedTime.value
        if (lastPausedTimeValue != 0L) {
            val elapsed = System.currentTimeMillis() - lastPausedTimeValue
            if (elapsed > AUTHENTICATION_DEADLINE) {
                isAuthenticated.value = false
                isAuthError.value = false
            }
        }
        isAuthError.value = false
    }

    private fun resetClient() {
        signalrController.resetClient()
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

    private fun handleAppDomain(uri: Uri) {
        if (uri.isSharedData()) {
            val screens = Screens(navController = navHostController, mainViewModel = mainViewModel)
            screens.getSharedContact(uri.toString())
        }
    }

    private fun handleArticlesDomain(uri: Uri) {
        val screens = Screens(navController = navHostController, mainViewModel = mainViewModel)
        screens.article(uri.toString(), null, null)
    }

    private fun handleWebDomain(uri: Uri) {
        handleArticlesDomain(uri.getArticleUri() ?: return)
    }
}
