package com.example.enigma.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.enigma.crypto.KeysManager
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.ui.navigation.SetupNavigation
import com.example.enigma.ui.themes.ApplicationComposeTheme
import com.example.enigma.util.NavigationTracker
import com.example.enigma.util.NotificationService
import com.example.enigma.viewmodels.MainViewModel
import com.example.enigma.workers.GraphReaderWorker
import com.example.enigma.workers.SignalRClientWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    init {
        System.loadLibrary("cryptography-wrapper")
    }

    @Inject lateinit var navigationTracker: NavigationTracker

    @Inject lateinit var notificationService: NotificationService

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
        SignalRClientWorker.startPeriodicSync(this)
        GraphReaderWorker.sync(this)

        observeGuardAvailability()
        observeClientConnectivity()
        observeNavigation()
    }

    override fun onResume() {
        super.onResume()
        manageNotifications(navigationTracker.currentRoute.value ?: Screens.NO_SCREEN)
    }

    override fun onPause() {
        super.onPause()
        manageNotifications(Screens.NO_SCREEN)
    }

    private val guardAvailabilityObserver = Observer<Boolean> { guardAvailable ->
        when(guardAvailable) {
            true -> {
                SignalRClientWorker.startConnection(this)
            }
            else -> {
                GraphReaderWorker.sync(this)
            }
        }
    }

    private val signalRStatusObserver = Observer<SignalRStatus> { clientStatus ->
        when(clientStatus) {
            is SignalRStatus.Disconnected ->
                SignalRClientWorker.startDelayedConnection(this)

            is SignalRStatus.NotConnected ->
                SignalRClientWorker.startConnection(this)

            is SignalRStatus.Error.ConnectionRefused -> {
                GraphReaderWorker.syncReplaceGuard(this)
                SignalRClientWorker.startDelayedConnection(this)
            }
        }
    }

    private val navigationObserver = Observer<String> { route -> manageNotifications(route) }

    private fun observeGuardAvailability()
    {
        mainViewModel.guardAvailable.observe(this, guardAvailabilityObserver)
    }

    private fun observeClientConnectivity()
    {
        mainViewModel.signalRClientStatus.observe(this, signalRStatusObserver)
    }

    private fun observeNavigation()
    {
        navigationTracker.currentRoute.observe(this, navigationObserver)
    }

    private fun enableNotifications()
    {
        notificationService.enableNotifications()
    }

    private fun disableNotifications()
    {
        notificationService.disableNotifications()
    }

    private fun disableNotifications(address: String)
    {
        notificationService.disableNotifications(address)
    }

    private fun dismissNotifications(address: String)
    {
        notificationService.dismissNotifications(address)
    }

    private fun manageNotifications(route: String)
    {
        if(NavigationTracker.isChatScreenRoute(route))
        {
            val chatId = Screens.getChatIdFromChatRoute(route) ?: return

            disableNotifications(chatId)
            dismissNotifications(chatId)
        }
        else if (NavigationTracker.isAddContactsScreenRoute(route))
        {
            enableNotifications()
        }
        else if (NavigationTracker.isContactsScreenRoute(route))
        {
            disableNotifications()
        }
        else {
            enableNotifications()
        }
    }
}
