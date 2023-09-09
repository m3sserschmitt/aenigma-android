package com.example.enigma.data.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SignalRClientWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalRClient: SignalRClient)
    : Worker(context, params) {

    companion object {
        init {
            System.loadLibrary("cryptography-wrapper")
        }
    }

    override fun doWork(): Result {

        Log.i("SIGNALR_WORKER", "Started")

        if(!signalRClient.isConnected())
        {
            signalRClient.start()
        }

        if(!signalRClient.isAuthenticated())
        {
            signalRClient.authenticate()
        }

        return Result.success()
    }
}
