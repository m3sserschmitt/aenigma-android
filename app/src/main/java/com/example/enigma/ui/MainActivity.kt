package com.example.enigma.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.enigma.crypto.KeysManager
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.ui.navigation.SetupNavigation
import com.example.enigma.ui.themes.ApplicationComposeTheme
import com.example.enigma.util.NavigationTracker
import com.example.enigma.util.NotificationService
import com.example.enigma.viewmodels.MainViewModel
import com.example.enigma.workers.GraphReaderWorker
import com.example.enigma.workers.MessageSenderWorker
import com.example.enigma.workers.SignalRClientWorker
import com.example.enigma.workers.SignalRWorkerAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

        KeysManager.generateKeyIfNotExistent(this)
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
        mainViewModel.signalRClientStatus.observe(this, signalRStatusObserver)
    }

    private fun observeOutgoingMessages() {
        mainViewModel.outgoingMessages.observe(this, outgoingMessagesObserver)
    }

    private fun observeNavigation() {
        navigationTracker.currentRoute.observe(this, navigationObserver)
    }

    private val outgoingMessagesObserver = Observer<List<MessageEntity>> { messages ->
        for (message in messages) {
            onOutgoingMessage(message)
        }
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
        }
        if (clientStatus greaterOrEqualThan SignalRStatus.Authenticated()) {
            onClientAuthenticated()
        }
    }

    private val navigationObserver = Observer<String> { route -> onScreenChanged(route) }

    private fun onClientAuthenticated() {
        observeOutgoingMessages()
    }

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

    private fun onOutgoingMessage(message: MessageEntity) {
        MessageSenderWorker.sendMessage(this, message)
    }

    private fun onScreenChanged(route: String) {
        if (NavigationTracker.isChatScreenRoute(route)) {
            val chatId = Screens.getChatIdFromChatRoute(route) ?: return

            disableNotifications(chatId)
            dismissNotifications(chatId)
        } else if (NavigationTracker.isAddContactsScreenRoute(route)) {
            enableNotifications()
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
