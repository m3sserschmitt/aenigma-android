package ro.aenigma.services

import android.annotation.SuppressLint
import android.content.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import org.torproject.jni.TorService
import ro.aenigma.R
import ro.aenigma.models.enums.TorStatus
import javax.inject.Singleton

@Singleton
class TorServiceMonitor @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val notificationService: NotificationService,
) {
    private val _torStatus = MutableStateFlow(TorStatus.IDLE)

    val torStatus: StateFlow<TorStatus> = _torStatus

    private val torStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra(TorService.EXTRA_STATUS)
            when (status) {
                TorService.STATUS_STARTING -> {
                    _torStatus.value = TorStatus.STARTING
                    notificationService.notifyTorStatus(appContext.getString(R.string.tor_starting))
                }

                TorService.STATUS_ON -> {
                    _torStatus.value = TorStatus.ON
                    notificationService.notifyTorStatus(appContext.getString(R.string.connected_through_tor))
                }

                TorService.STATUS_OFF -> {
                    _torStatus.value = TorStatus.OFF
                    notificationService.notifyTorStatus(appContext.getString(R.string.tor_stopped))
                }
            }
        }
    }

    init {
        val intentFilter = IntentFilter(TorService.ACTION_STATUS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(torStatusReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(torStatusReceiver, intentFilter)
        }
    }
}
