package ro.aenigma.services

import android.annotation.SuppressLint
import android.content.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.torproject.jni.TorService
import ro.aenigma.R
import ro.aenigma.data.Repository
import ro.aenigma.models.enums.TorCircuitState
import ro.aenigma.models.enums.TorStatus
import ro.aenigma.util.Constants
import ro.aenigma.util.Constants.Companion.CHECK_TOR_URL
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Singleton

@Singleton
class OnionRoutingServiceMonitor @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val repository: Repository,
    private val notifier: Notifier,
) {
    companion object {
        @JvmStatic
        fun isHostListening(host: String, port: Int, timeout: Int = 1000): Boolean {
            return try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeout)
                    true
                }
            } catch (_: Exception) {
                false
            }
        }

        @JvmStatic
        suspend fun checkProxy(
            maxAttempts: Int = 10,
            delayMs: Long = 3000
        ): Boolean {
            repeat(maxAttempts) {
                if (isHostListening(
                        Constants.TOR_PROXY_HOSTNAME,
                        Constants.TOR_SOCKS5_PROXY_PORT
                    )
                ) {
                    return true
                }
                delay(delayMs)
            }
            return false
        }
    }

    private val _torCircuitState = MutableStateFlow(TorCircuitState.UNDEFINED)

    private val _torStatus = MutableStateFlow(TorStatus.OFF)

    val torStatus: StateFlow<TorStatus> = _torStatus

    val torCircuitState: StateFlow<TorCircuitState> = _torCircuitState

    private val torStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra(TorService.EXTRA_STATUS)
            when (status) {
                TorService.STATUS_STARTING -> {
                    _torStatus.value = TorStatus.STARTING
                    notifier.notifyTorStatus(appContext.getString(R.string.tor_starting))
                }

                TorService.STATUS_ON -> {
                    _torStatus.value = TorStatus.ON
                    notifier.notifyTorStatus(appContext.getString(R.string.connected_through_tor))
                }

                TorService.STATUS_OFF -> {
                    _torStatus.value = TorStatus.OFF
                    _torCircuitState.value = TorCircuitState.NOT_OK
                    notifier.notifyTorStatus(appContext.getString(R.string.tor_stopped))
                }
            }
        }
    }

    fun observeSocksProxy(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            combine(
                repository.local.useTor,
                repository.local.useOrbot,
            ) { torPreference, orbotPreference ->
                torPreference || orbotPreference
            }.distinctUntilChanged().collect { useTor ->
                if (useTor) {
                    if (checkProxy()) {
                        _torStatus.value = TorStatus.ON
                        queryTorConnection()
                    }
                } else {
                    _torStatus.value = TorStatus.OFF
                    _torCircuitState.value = TorCircuitState.NOT_OK
                }
            }
        }
    }

    private suspend fun queryTorConnection() {
        _torCircuitState.value =
            if (repository.remote.checkTor(CHECK_TOR_URL)?.isTor ?: false) {
                TorCircuitState.OK
            } else {
                TorCircuitState.NOT_OK
            }
    }

    init {
        val intentFilter = IntentFilter(TorService.ACTION_STATUS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                torStatusReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(torStatusReceiver, intentFilter)
        }
    }
}
