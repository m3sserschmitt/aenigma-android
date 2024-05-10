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
import com.example.enigma.ui.navigation.SetupNavigation
import com.example.enigma.ui.themes.ApplicationComposeTheme
import com.example.enigma.viewmodels.MainViewModel
import com.example.enigma.workers.GraphReaderWorker
import com.example.enigma.workers.SignalRClientWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    init {
        System.loadLibrary("cryptography-wrapper")
    }

    private lateinit var navController: NavHostController

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ApplicationComposeTheme {
                navController = rememberNavController()
                SetupNavigation(
                    navHostController = navController,
                    mainViewModel = mainViewModel
                )
            }
        }

        KeysManager.generateKeyIfNotExistent(this)
        SignalRClientWorker.startPeriodicSync(this)
        GraphReaderWorker.sync(this)

        observeGuardAvailability()
        observerClientConnectivity()
    }

    override fun onResume() {
        super.onResume()
        observerClientConnectivity()
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

    private fun observeGuardAvailability()
    {
        mainViewModel.guardAvailable.observe(this, guardAvailabilityObserver)
    }

    private fun observerClientConnectivity()
    {
        mainViewModel.signalRClientStatus.observe(this, signalRStatusObserver)
    }
}
