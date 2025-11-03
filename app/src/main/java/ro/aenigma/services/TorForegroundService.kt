package ro.aenigma.services

import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import org.torproject.jni.TorService
import ro.aenigma.R
import javax.inject.Inject

@AndroidEntryPoint
class TorForegroundService @Inject constructor(): TorService() {
    companion object {
        const val START_FOREGROUND_INTENT_EXTRA = "start-foreground"
    }

    @Inject
    lateinit var notificationService: NotificationService

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startForeground = intent?.getBooleanExtra(START_FOREGROUND_INTENT_EXTRA, true) != false
        if(startForeground) {
            val notification =
                notificationService.createTorServiceNotification(getString(R.string.tor_starting))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NotificationService.TOR_NOTIFICATION_ID,
                    notification,
                    FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NotificationService.TOR_NOTIFICATION_ID, notification)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
