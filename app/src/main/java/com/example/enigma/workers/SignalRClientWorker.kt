package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.enigma.data.network.SignalRClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SignalRClientWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalRClient: SignalRClient
)
    : CoroutineWorker(context, params) {

    companion object {
        init {
            System.loadLibrary("cryptography-wrapper")
        }
    }

    override suspend fun doWork(): Result
    {
        if(!signalRClient.isConnected())
        {
            signalRClient.createConnection()
        }

        return Result.success()
    }
}
