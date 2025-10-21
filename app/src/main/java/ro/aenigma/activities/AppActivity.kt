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
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
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
import ro.aenigma.util.Constants.Companion.APP_DOMAIN
import ro.aenigma.util.Constants.Companion.ARTICLES_DOMAIN
import ro.aenigma.util.Constants.Companion.WEB_DOMAIN
import ro.aenigma.viewmodels.MainViewModel
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import javax.inject.Inject
import androidx.core.net.toUri

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

    private val isAuthError = MutableStateFlow(false)

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var navHostController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.Companion.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.Companion.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        loadDbPassphrase()
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        setContent {
            ApplicationComposeTheme {
                val auth by isAuthenticated.collectAsState()
                val authError by isAuthError.collectAsState()
                val passphraseLoaded by dbPassphraseLoaded.collectAsState()
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
                        observeTorService()
                        observeClientConnectivity()
                        observeNavigation()
                        handleAppLink()
                        schedulePeriodicSync()
                    }

                    SetupNavigation(
                        navigationTracker = navigationTracker,
                        navHostController = navHostController,
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
        isAuthError.value = false
        onScreenChanged(navigationTracker.currentRoute.value ?: Screens.Companion.NO_SCREEN)
        if (dbPassphraseLoaded.value) {
            resetClient()
            sync()
        }
    }

    override fun onPause() {
        super.onPause()
        onScreenChanged(Screens.Companion.NO_SCREEN)
    }

    private fun sync() {
        SignalRClientWorker.Companion.start(
            this,
            actions = SignalRWorkerAction.Pull() and SignalRWorkerAction.Cleanup()
        )
    }

    private fun resetClient() {
        signalrConnectionController.resetClient()
    }

    private fun schedulePeriodicSync() {
        SignalRClientWorker.Companion.schedulePeriodicSync(this)
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
        if (NavigationTracker.Companion.isChatScreenRoute(route)) {
            val chatId = Screens.Companion.getChatIdFromChatRoute(route) ?: return

            disableNotifications(chatId)
            dismissNotifications(chatId)
        } else if (NavigationTracker.Companion.isContactsScreenRoute(route)) {
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
        val appLinkData = intent.data ?: return
        val domain = appLinkData.host?.lowercase() ?: return
        if (intent.action != Intent.ACTION_VIEW) {
            return
        }
        when(domain) {
            APP_DOMAIN -> handleAppDomain(appLinkData)
            ARTICLES_DOMAIN -> handleArticlesDomain(appLinkData)
            WEB_DOMAIN -> handleWebLink(appLinkData)
        }
    }

    private fun handleAppDomain(uri: Uri) {
        if(uri.path?.lowercase() == "/share") {
            mainViewModel.openContactSharedData(uri.toString())
        }
    }

    private fun handleArticlesDomain(uri: Uri) {
        val stringUri = uri.toString()
        if(stringUri.lowercase().endsWith(".md")) {
            val screens = Screens(navHostController)
            screens.article(stringUri)
        }
    }

    private fun handleWebLink(uri: Uri) {
        val regex = Regex("[?&]url=([^&#]+)")
        val match = regex.find(uri.toString().lowercase())
        val encodedValue = match?.groups?.get(1)?.value
        val decodedValue = encodedValue?.let { Uri.decode(it) }
        val finalUri = decodedValue?.toUri() ?: return
        handleArticlesDomain(finalUri)
    }
}
