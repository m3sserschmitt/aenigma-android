package ro.aenigma.services

import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import org.torproject.jni.TorService
import ro.aenigma.R
import javax.inject.Inject

@AndroidEntryPoint
class OnionRoutingService @Inject constructor(): TorService() {
    @Inject
    lateinit var notifier: Notifier

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification =
            notifier.createTorServiceNotification(getString(R.string.tor_starting))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Notifier.TOR_NOTIFICATION_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            )
        } else {
            startForeground(Notifier.TOR_NOTIFICATION_ID, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
