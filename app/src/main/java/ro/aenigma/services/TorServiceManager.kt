package ro.aenigma.services

import android.annotation.SuppressLint
import android.content.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import org.torproject.jni.TorService
import ro.aenigma.services.TorForegroundService.Companion.START_FOREGROUND_INTENT_EXTRA
import javax.inject.Singleton

@Singleton
class TorServiceManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationService: NotificationService,
) {
    companion object {
        private const val TOR_IS_STARTING_MESSAGE = "TOR is starting"
        private const val TOR_STARTED_MESSAGE = "TOR started"
        private const val TOR_STOPPED_MESSAGE = "TOR stopped"
    }

    private val _torStatus = MutableStateFlow<TorStatus>(TorStatus.Idle)

    val torStatus: StateFlow<TorStatus> = _torStatus

    private val torStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra(TorService.EXTRA_STATUS)
            when (status) {
                TorService.STATUS_STARTING -> {
                    _torStatus.value = TorStatus.Starting
                    notificationService.notifyTorStatus(TOR_IS_STARTING_MESSAGE)
                }

                TorService.STATUS_ON -> {
                    _torStatus.value = TorStatus.On
                    notificationService.notifyTorStatus(TOR_STARTED_MESSAGE)
                }

                TorService.STATUS_OFF -> {
                    _torStatus.value = TorStatus.Off
                    notificationService.notifyTorStatus(TOR_STOPPED_MESSAGE)
                }
            }
        }
    }

    init {
        val intentFilter = IntentFilter(TorService.ACTION_STATUS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(torStatusReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(torStatusReceiver, intentFilter)
        }
    }

    fun start(startForeground: Boolean = true) {
        val currentStatus = _torStatus.value
        if (currentStatus == TorStatus.On || currentStatus == TorStatus.Starting) {
            return
        }

        val intent = Intent(context, TorForegroundService::class.java)
        intent.action = "ON"
        intent.putExtra(START_FOREGROUND_INTENT_EXTRA, startForeground)
        context.startService(intent)
    }

    fun stop() {
        val intent = Intent(context, TorForegroundService::class.java)
        intent.action = "OFF"
        context.stopService(intent)
    }

    fun destroy() {
        try {
            context.unregisterReceiver(torStatusReceiver)
        } catch (_: Exception) {
        }
    }
}
