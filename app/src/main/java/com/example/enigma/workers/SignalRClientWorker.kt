package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.enigma.data.Repository
import com.example.enigma.data.network.SignalRClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SignalRClientWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalRClient: SignalRClient,
    private val repository: Repository
)
    : CoroutineWorker(context, params) {

    companion object {
        init {
            System.loadLibrary("cryptography-wrapper")
        }

        private const val PERIODIC_WORK_REQUEST = "SIGNALR_PERIODIC_CONNECTION"
        private const val INITIAL_PERIODIC_WORK_DELAY: Long = 10
        private const val PERIODIC_WORK_REPEAT_INTERNAL: Long = 15

        @JvmStatic
        fun startPeriodicSync(context: Context)
        {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val signalRClientRequest = PeriodicWorkRequest.Builder(
                SignalRClientWorker::class.java,
                PERIODIC_WORK_REPEAT_INTERNAL,
                TimeUnit.MINUTES
            )
                .setInitialDelay(INITIAL_PERIODIC_WORK_DELAY, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_REQUEST,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    signalRClientRequest
                )
        }

        @JvmStatic
        fun startConnection(context: Context)
        {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val signalRClientRequest = OneTimeWorkRequest.Builder(SignalRClientWorker::class.java)
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(signalRClientRequest)
        }
    }

    override suspend fun doWork(): Result
    {
        val guard = repository.local.getGuard() ?: return Result.failure()

        if(!signalRClient.isConnected())
        {
            signalRClient.createConnection(guard.hostname)
        }

        return Result.success()
    }
}
