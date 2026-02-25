package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ro.aenigma.data.Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.R
import ro.aenigma.services.NotificationService
import ro.aenigma.services.SignalrController
import ro.aenigma.util.Constants.Companion.SIGNALR_NOTIFICATION_ID
import java.util.concurrent.TimeUnit

@HiltWorker
class SignalRClientWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalrController: SignalrController,
    private val repository: Repository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, params) {

    companion object {
        private const val ACTION_ARG = "Action"
        private const val UNIQUE_ONE_TIME_REQUEST = "SIGNALR_ONE_TIME_REQUEST"
        private const val UNIQUE_PERIODIC_WORK_REQUEST = "SIGNALR_PERIODIC_CONNECTION"
        private const val PERIODIC_WORK_REPEAT_INTERVAL: Long = 15 // Minutes
        private const val DELAY_BETWEEN_RETRIES: Long = 5 // Seconds
        private const val MAX_RETRY_COUNT = 5 // Seconds

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
                .setConstraints(constraints)
                .setInputData(parameters)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK_REQUEST,
                    ExistingPeriodicWorkPolicy.KEEP,
                    signalRClientRequest
                )
        }

        @JvmStatic
        fun createRequest(
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
                ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            return workRequest.build()
        }

        @JvmStatic
        fun start(
            context: Context,
            actions: SignalRWorkerAction = SignalRWorkerAction.connectPullCleanup(),
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_ONE_TIME_REQUEST,
                    ExistingWorkPolicy.KEEP,
                    createRequest(actions)
                )
        }
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }
        val action = SignalRWorkerAction(inputData.getInt(ACTION_ARG, 0))
        var ok = true
        if (signalrController.isConnected() && action contains SignalRWorkerAction.Disconnect()) {
            ok = ok && signalrController.disconnect()
        }

        if (!signalrController.isConnected() && action contains SignalRWorkerAction.Connect()) {
            ok = ok && signalrController.connect(
                repository.local.getHostname() ?: return Result.failure()
            )
        }

        if (signalrController.isConnected() && action contains SignalRWorkerAction.Pull()) {
            ok = ok && signalrController.pull()
        }

        if (signalrController.isConnected() && action contains SignalRWorkerAction.Cleanup()) {
            ok = ok && signalrController.cleanup()
        }

        return if (ok) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ForegroundInfo(
            SIGNALR_NOTIFICATION_ID,
            notificationService.createWorkerNotification(applicationContext.getString(R.string.connecting_server)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                SIGNALR_NOTIFICATION_ID,
                notificationService.createWorkerNotification(applicationContext.getString(R.string.connecting_server))
            )
    }
}
