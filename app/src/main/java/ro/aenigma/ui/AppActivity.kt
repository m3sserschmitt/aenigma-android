package ro.aenigma.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ro.aenigma.services.NavigationTracker
import ro.aenigma.services.NotificationService
import ro.aenigma.services.SignalrConnectionController
import ro.aenigma.services.TorServiceController
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.navigation.SetupNavigation
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.viewmodels.MainViewModel
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : ComponentActivity() {

    @Inject
    lateinit var torServiceController: TorServiceController

    @Inject
    lateinit var signalrConnectionController: SignalrConnectionController

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
        observeTorService()
        observeClientConnectivity()
        observeNavigation()
        handleAppLink()
        schedulePeriodicSync()
//        schedulePeriodicCleanup()
    }

    override fun onResume() {
        super.onResume()
        onScreenChanged(navigationTracker.currentRoute.value ?: Screens.NO_SCREEN)
        resetClient()
        sync()
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

//    private fun schedulePeriodicCleanup() {
//        CleanupWorker.scheduleCleanup(this)
//    }

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
