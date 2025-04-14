package ro.aenigma.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ro.aenigma.data.network.SignalRStatus
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.navigation.SetupNavigation
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.services.NavigationTracker
import ro.aenigma.services.NotificationService
import ro.aenigma.viewmodels.MainViewModel
import ro.aenigma.workers.GraphReaderWorker
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import dagger.hilt.android.AndroidEntryPoint
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.workers.CleanupWorker
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : ComponentActivity() {

    @Inject
    lateinit var signalRClient: SignalRClient

    @Inject
    lateinit var navigationTracker: NavigationTracker

    @Inject
    lateinit var notificationService: NotificationService

    private lateinit var navController: NavHostController

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ApplicationComposeTheme {
                navController = rememberNavController()
                SetupNavigation(
                    navigationTracker = navigationTracker,
                    navHostController = navController,
                    mainViewModel = mainViewModel
                )
            }
        }

        startConnection()
        observeClientConnectivity()
        observeNavigation()
        handleAppLink()
    }

    override fun onResume() {
        super.onResume()
        onScreenChanged(navigationTracker.currentRoute.value ?: Screens.NO_SCREEN)
        SignalRClientWorker.start(
            this,
            actions = SignalRWorkerAction.Pull() and SignalRWorkerAction.Cleanup()
        )
    }

    override fun onPause() {
        super.onPause()
        onScreenChanged(Screens.NO_SCREEN)
    }

    override fun onStop() {
        super.onStop()
        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        SignalRClientWorker.schedulePeriodicSync(this)
    }

    private fun startConnection() {
        val syncGraphWorkRequest = GraphReaderWorker.createSyncRequest()
        val startConnectionWorkRequest = SignalRClientWorker.createRequest(
            actions = SignalRWorkerAction.connectPullCleanup() and SignalRWorkerAction.Broadcast()
        )
        WorkManager.getInstance(this).beginWith(syncGraphWorkRequest)
            .then(startConnectionWorkRequest)
            .enqueue()
    }

    private fun observeClientConnectivity() {
        signalRClient.status.observe(this, signalRStatusObserver)
    }

    private fun observeNavigation() {
        navigationTracker.currentRoute.observe(this, navigationObserver)
    }

    private val signalRStatusObserver = Observer<SignalRStatus> { clientStatus ->
        when (clientStatus) {
            is SignalRStatus.Reset -> {
                onSignalRClientReset()
            }

            is SignalRStatus.Error.Disconnected -> {
                onClientDisconnected()
            }

            is SignalRStatus.Error.ConnectionRefused -> {
                onClientConnectionRefused()
            }

            is SignalRStatus.Error -> {
                onClientError()
            }

            is SignalRStatus.Synchronized -> {
                val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>().build()
                WorkManager.getInstance(this).enqueue(cleanupRequest)
            }
        }
    }

    private val navigationObserver = Observer<String> { route -> onScreenChanged(route) }

    private fun onClientConnectionRefused() {
        SignalRClientWorker.startDelayed(this)
    }

    private fun onClientDisconnected() {
        SignalRClientWorker.startDelayed(this)
    }

    private fun onClientError() {
        SignalRClientWorker.startDelayed(
            this,
            SignalRWorkerAction.Disconnect() and SignalRWorkerAction.connectPullCleanup()
        )
    }

    private fun onSignalRClientReset() {
        SignalRClientWorker.startDelayed(this)
    }

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
