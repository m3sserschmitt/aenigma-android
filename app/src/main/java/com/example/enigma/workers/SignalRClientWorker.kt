package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
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
) : CoroutineWorker(context, params) {

    companion object {
        init {
            System.loadLibrary("cryptography-wrapper")
        }

        private const val UNIQUE_ONE_TIME_REQUEST = "SIGNALR_ONE_TIME_REQUEST"
        private const val UNIQUE_PERIODIC_WORK_REQUEST = "SIGNALR_PERIODIC_CONNECTION"
        private const val INITIAL_PERIODIC_WORK_DELAY: Long = 10 // Minutes
        private const val PERIODIC_WORK_REPEAT_INTERVAL: Long = 15 // Minutes
        private const val DELAY_BETWEEN_RETRIES: Long = 3 // Seconds
        private const val MAX_RETRY_COUNT = 5 // Seconds

        @JvmStatic
        fun schedulePeriodicSync(context: Context)
        {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val signalRClientRequest = PeriodicWorkRequest.Builder(
                SignalRClientWorker::class.java,
                PERIODIC_WORK_REPEAT_INTERVAL,
                TimeUnit.MINUTES
            )
                .setInitialDelay(INITIAL_PERIODIC_WORK_DELAY, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK_REQUEST,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    signalRClientRequest
                )
        }

        @JvmStatic
        fun createConnectionRequest(initialDelay: Long = 0): OneTimeWorkRequest
        {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequest.Builder(SignalRClientWorker::class.java)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    DELAY_BETWEEN_RETRIES,
                    TimeUnit.SECONDS
                )

            if(initialDelay > 0)
            {
                workRequest.setInitialDelay(initialDelay, TimeUnit.SECONDS)
            }
            else
            {
                workRequest.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }

            return workRequest.build()
        }

        @JvmStatic
        fun startConnection(
            context: Context,
            initialDelay: Long = 0
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_ONE_TIME_REQUEST,
                    ExistingWorkPolicy.KEEP,
                    createConnectionRequest(initialDelay)
                )
        }

        @JvmStatic
        fun startDelayedConnection(context: Context)
        {
            startConnection(context, DELAY_BETWEEN_RETRIES)
        }
    }

    override suspend fun doWork(): Result
    {
        val guard = repository.local.getGuard()

        if(guard == null && runAttemptCount < MAX_RETRY_COUNT)
        {
            return Result.retry()
        }
        else if(guard == null)
        {
            return Result.failure()
        }

        if(!signalRClient.isConnected())
        {
            signalRClient.createConnection(guard.hostname)
        }

        return Result.success()
    }
}
