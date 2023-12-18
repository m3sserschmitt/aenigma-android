package com.example.enigma.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.enigma.R
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

        startRequestGraph()
        setupNavigation()
        startSignalRWorker()
        startKeyGeneratorWorker()
    }

    private fun startSignalRWorker()
    {
        mainViewModel.keysAvailable.observe(this)
        {
            if(it)
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
        }
    }

    private fun startKeyGeneratorWorker()
    {
        mainViewModel.keysAvailable.observe(this) {
            if(!it)
            {
                val keyGeneratorRequest = OneTimeWorkRequestBuilder<KeysGeneratorWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()

                WorkManager.getInstance(this).enqueue(keyGeneratorRequest)
            }
        }
    }

    private fun startRequestGraph()
    {
        mainViewModel.guardAvailable.observe(this)
        {
            if (!it) {
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<GraphReaderWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(this).enqueue(workRequest)
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
