package ro.aenigma.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ro.aenigma.workers.extensions.WorkManagerExtensions.invokeClient
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@AndroidEntryPoint
class NotificationService @Inject constructor(): Service() {

    companion object {
        val SYNC_INTERVAL = 15.minutes
        const val ACTION_START = "ON"
        const val ACTION_STOP = "OFF"
    }

    @Inject
    lateinit var notifier: Notifier

    @Inject
    lateinit var workManager: WorkManager

    private val tickerCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var tickerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    private fun tickerFlow(interval: Duration) = flow {
        while (true) {
            emit(Unit)
            delay(interval)
        }
    }

    private fun startTimer() {
        stopTimer()
        tickerJob = tickerFlow(SYNC_INTERVAL).onEach {
            workManager.invokeClient(actions = ClientAction.connectPullCleanup())
        }.launchIn(tickerCoroutineScope)
    }

    private fun stopTimer() {
        tickerJob?.cancel()
    }

    private fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Notifier.NOTIFICATION_SERVICE_NOTIFICATION_ID,
                notifier.createNotificationServiceNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            )
        } else {
            startForeground(
                Notifier.NOTIFICATION_SERVICE_NOTIFICATION_ID,
                notifier.createNotificationServiceNotification()
            )
        }
        startTimer()
    }

    private fun stop() {
        stopTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
