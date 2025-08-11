package ro.aenigma.ui

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ro.aenigma.data.PreferencesDataStore
import ro.aenigma.di.DbPassphraseKeeper
import ro.aenigma.services.NavigationTracker
import ro.aenigma.services.NotificationService
import ro.aenigma.services.SignalrConnectionController
import ro.aenigma.services.TorServiceController
import ro.aenigma.ui.biometric.SecuredApp
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.navigation.SetupNavigation
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.viewmodels.MainViewModel
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : FragmentActivity() {

    @Inject
    lateinit var torServiceController: TorServiceController

    @Inject
    lateinit var signalrConnectionController: SignalrConnectionController

    @Inject
    lateinit var navigationTracker: NavigationTracker

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    private val dbPassphraseLoaded = MutableStateFlow(false)

    private val isAuthenticated = MutableStateFlow(false)

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDbPassphrase()
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        setContent {
            ApplicationComposeTheme {
                val auth by isAuthenticated.collectAsState()
                SecuredApp(
                    isDeviceSecured = keyguardManager.isDeviceSecure,
                    isAuthenticated = auth,
                    onAuthSuccess = { isAuthenticated.value = true },
                    dbPassphraseLoaded = dbPassphraseLoaded,
                ) {
                    val navController = rememberNavController()

                    LaunchedEffect(key1 = true) {
                        observeTorService()
                        observeClientConnectivity()
                        observeNavigation()
                        handleAppLink()
                        schedulePeriodicSync()
                    }

                    SetupNavigation(
                        navigationTracker = navigationTracker,
                        navHostController = navController,
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
        isAuthenticated.value = false
        onScreenChanged(navigationTracker.currentRoute.value ?: Screens.NO_SCREEN)
        if (dbPassphraseLoaded.value) {
            resetClient()
            sync()
        }
    }

    override fun onPause() {
        super.onPause()
        onScreenChanged(Screens.NO_SCREEN)
    }

    private fun sync() {
        SignalRClientWorker.start(
            this,
            actions = SignalRWorkerAction.Pull() and SignalRWorkerAction.Cleanup()
        )
    }

    private fun resetClient() {
        signalrConnectionController.resetClient()
    }

    private fun schedulePeriodicSync() {
        SignalRClientWorker.schedulePeriodicSync(this)
    }

    private fun observeTorService() {
        return torServiceController.observeTorService(this)
    }

    private fun observeClientConnectivity() {
        return signalrConnectionController.observeSignalrConnection(this)
    }

    private fun observeNavigation() {
        navigationTracker.currentRoute.observe(this, navigationObserver)
    }

    private val navigationObserver = Observer<String> { route -> onScreenChanged(route) }

    private fun onScreenChanged(route: String) {
        if (NavigationTracker.isChatScreenRoute(route)) {
            val chatId = Screens.getChatIdFromChatRoute(route) ?: return

            disableNotifications(chatId)
            dismissNotifications(chatId)
        } else if (NavigationTracker.isContactsScreenRoute(route)) {
            disableNotifications()
        } else {
            enableNotifications()
        }
    }

    private fun enableNotifications() {
        notificationService.enableNotifications()
    }

    private fun disableNotifications() {
        notificationService.disableNotifications()
    }

    private fun disableNotifications(address: String) {
        notificationService.disableNotifications(address)
    }

    private fun dismissNotifications(address: String) {
        notificationService.dismissNotifications(address)
    }

    private fun handleAppLink() {
        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action ?: return
        val appLinkData = appLinkIntent.data ?: return
        val path = appLinkData.path ?: return
        if (appLinkAction != Intent.ACTION_VIEW || path.lowercase() != "/share") {
            return
        }

        mainViewModel.openContactSharedData(appLinkData.toString())
    }
}
