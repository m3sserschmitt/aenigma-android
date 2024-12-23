package ro.aenigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ro.aenigma.data.Repository
import ro.aenigma.data.network.SignalRClient
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
        private const val ACTION_ARG = "Action"
        private const val UNIQUE_ONE_TIME_REQUEST = "SIGNALR_ONE_TIME_REQUEST"
        private const val UNIQUE_PERIODIC_WORK_REQUEST = "SIGNALR_PERIODIC_CONNECTION"
        private const val INITIAL_PERIODIC_WORK_DELAY: Long = 10 // Minutes
        private const val PERIODIC_WORK_REPEAT_INTERVAL: Long = 15 // Minutes
        private const val DELAY_BETWEEN_RETRIES: Long = 3 // Seconds
        private const val MAX_RETRY_COUNT = 3 // Seconds

        @JvmStatic
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val actions = SignalRWorkerAction.Connect() and SignalRWorkerAction.Pull()
            val parameters = Data.Builder().putInt(ACTION_ARG, actions.value).build()

            val signalRClientRequest = PeriodicWorkRequest.Builder(
                SignalRClientWorker::class.java,
                PERIODIC_WORK_REPEAT_INTERVAL,
                TimeUnit.MINUTES
            )
                .setInitialDelay(INITIAL_PERIODIC_WORK_DELAY, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(parameters)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK_REQUEST,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    signalRClientRequest
                )
        }

        @JvmStatic
        fun createRequest(
            initialDelay: Long = 0,
            actions: SignalRWorkerAction = SignalRWorkerAction.connectPullCleanup()
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val parameters = Data.Builder()
                .putInt(ACTION_ARG, actions.value)
                .build()

            val workRequest = OneTimeWorkRequest.Builder(SignalRClientWorker::class.java)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    DELAY_BETWEEN_RETRIES,
                    TimeUnit.SECONDS
                )

            if (initialDelay > 0) {
                workRequest.setInitialDelay(initialDelay, TimeUnit.SECONDS)
            } else {
                workRequest.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }

            return workRequest.build()
        }

        @JvmStatic
        fun start(
            context: Context,
            actions: SignalRWorkerAction = SignalRWorkerAction.connectPullCleanup(),
            initialDelay: Long = 0
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_ONE_TIME_REQUEST,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    createRequest(initialDelay, actions)
                )
        }

        @JvmStatic
        fun startDelayed(
            context: Context,
            actions: SignalRWorkerAction = SignalRWorkerAction.connectPullCleanup()
        ) {
            start(context, actions, DELAY_BETWEEN_RETRIES)
        }
    }

    override suspend fun doWork(): Result {
        val guard = repository.local.getGuard()

        if (guard == null && runAttemptCount < MAX_RETRY_COUNT) {
            return Result.retry()
        } else if (guard == null) {
            return Result.failure()
        }

        val action = SignalRWorkerAction(inputData.getInt(ACTION_ARG, 0))

        if (signalRClient.isConnected() && action contains SignalRWorkerAction.Disconnect()) {
            signalRClient.disconnect()
        }

        if (!signalRClient.isConnected() && action contains SignalRWorkerAction.Connect()) {
            signalRClient.connect(guard.hostname, guard.address)
        }

        if (signalRClient.isConnected() && action contains SignalRWorkerAction.Pull()) {
            signalRClient.pull()
        }

        if (signalRClient.isConnected() && action contains SignalRWorkerAction.Broadcast()) {
            signalRClient.broadcast()
        }

        if (signalRClient.isConnected() && action contains SignalRWorkerAction.Cleanup()) {
            signalRClient.cleanup()
        }

        return Result.success()
    }
}
