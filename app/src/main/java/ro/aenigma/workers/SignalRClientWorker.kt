package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ro.aenigma.data.Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.R
import ro.aenigma.services.Notifier
import ro.aenigma.services.SignalrController
import ro.aenigma.util.Constants.Companion.SIGNALR_NOTIFICATION_ID
import java.util.concurrent.TimeUnit

@HiltWorker
class SignalRClientWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalrController: SignalrController,
    private val repository: Repository,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {

    companion object {
        const val ACTION_ARG = "action"
        const val UNIQUE_ONE_TIME_REQUEST = "signalr-client-worker"
        const val UNIQUE_PERIODIC_WORK_REQUEST = "SIGNALR_PERIODIC_CONNECTION"
        const val PERIODIC_WORK_REPEAT_INTERVAL: Long = 15
        val PERIODIC_WORK_REQUEST_TIME_UNIT = TimeUnit.MINUTES
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }
        val action = SignalRWorkerAction(inputData.getInt(ACTION_ARG, 0))
        var ok = true
        if (signalrController.isConnected() && action contains SignalRWorkerAction.Disconnect()) {
            ok = signalrController.disconnect()
        }

        if (ok && !signalrController.isConnected() && action contains SignalRWorkerAction.Connect()) {
            ok = signalrController.connect(repository.local.getGuardHostname())
        }

        if (ok && signalrController.isAuthenticated() && action contains SignalRWorkerAction.Pull()) {
            ok = signalrController.pull()
        }

        if (ok && signalrController.isAuthenticated() && action contains SignalRWorkerAction.Cleanup()) {
            ok = signalrController.cleanup()
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
            notifier.createWorkerNotification(applicationContext.getString(R.string.connecting_server)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                SIGNALR_NOTIFICATION_ID,
                notifier.createWorkerNotification(applicationContext.getString(R.string.connecting_server))
            )
    }
}
