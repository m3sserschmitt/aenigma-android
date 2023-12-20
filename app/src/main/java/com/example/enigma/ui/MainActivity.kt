package com.example.enigma.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.enigma.R
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.viewmodels.MainViewModel
import com.example.enigma.workers.GraphReaderWorker
import com.example.enigma.workers.KeysGeneratorWorker
import com.example.enigma.workers.SignalRClientWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    companion object
    {
        init {
            System.loadLibrary("cryptography-wrapper")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupNavigation()
        setupConnectionStatus()
        startWorkers()
    }

    private fun startWorkers()
    {
        mainViewModel.keysAvailable.observe(this){ keysAvailable ->
            if(!keysAvailable)
            {
                startKeyGeneratorWorker()
            } else {
                mainViewModel.guardAvailable.observe(this) { guardAvailable ->
                    if(!guardAvailable)
                    {
                        startRequestGraph()
                    } else {
                        startSignalRWorker()
                    }
                }
            }
        }
    }

    private fun startSignalRWorker()
    {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val signalRClientRequest =
            PeriodicWorkRequestBuilder<SignalRClientWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this)
            .enqueue(signalRClientRequest)
    }

    private fun startKeyGeneratorWorker()
    {
        val keyGeneratorRequest = OneTimeWorkRequestBuilder<KeysGeneratorWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(this).enqueue(keyGeneratorRequest)
    }

    private fun startRequestGraph()
    {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<GraphReaderWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun setupConnectionStatus()
    {
        // TODO: We shall refactor this to use something more sophisticated than Toasts!
        mainViewModel.signalRClientStatus.observe(this) {
            when(it) {

                is SignalRStatus.Connected ->
                    Toast.makeText(this, "Connected to server.",
                        Toast.LENGTH_SHORT).show()

                is SignalRStatus.Disconnected ->
                    Toast.makeText(this, "Server connection lost.",
                        Toast.LENGTH_SHORT).show()

                is SignalRStatus.Error ->
                    Toast.makeText(this, "Failed to connect: ${it.error}.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigation()
    {
        val navController = this.findNavController(R.id.navHostFragment)
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navView.setupWithNavController(navController)
    }
}
