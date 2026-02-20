package ro.aenigma.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ro.aenigma.data.Repository
import ro.aenigma.models.enums.TorConnectionCheck
import ro.aenigma.models.enums.TorStatus
import ro.aenigma.services.TorServiceImpl.Companion.START_FOREGROUND_INTENT_EXTRA
import ro.aenigma.util.Constants.Companion.CHECK_TOR_URL
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_HOSTNAME
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_PORT
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorController @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val torServiceMonitor: TorServiceMonitor,
    private val repository: Repository
) {
    companion object {
        @JvmStatic
        private suspend fun isSocks5Listening(
            host: String,
            port: Int,
            timeoutMs: Int = 1000
        ): Boolean = withContext(Dispatchers.IO) {
            runCatching {
                Socket().use { socket ->
                    socket.soTimeout = timeoutMs
                    socket.connect(InetSocketAddress(host, port), timeoutMs)

                    val hello = byteArrayOf(0x05, 0x01, 0x00)
                    socket.getOutputStream().apply {
                        write(hello)
                        flush()
                    }

                    val resp = ByteArray(2)
                    var read = 0
                    val input = socket.getInputStream()
                    while (read < 2) {
                        val r = input.read(resp, read, 2 - read)
                        if (r == -1) break
                        read += r
                    }

                    read == 2 && resp[0] == 0x05.toByte() && resp[1] == 0x00.toByte()
                }
            }.getOrDefault(false)
        }
    }

    private val _torConnectionCheck = MutableStateFlow(TorConnectionCheck.UNDEFINED)

    val torConnectionCheck: StateFlow<TorConnectionCheck> = _torConnectionCheck

    fun observeTorPreferences(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            combine(
                repository.local.useTor,
                repository.local.useOrbot,
                torServiceMonitor.torStatus
            ) { useTor, useOrbot, torStatus ->
                Triple(useTor, useOrbot, torStatus)
            }.distinctUntilChanged().collect { (useTor, useOrbot, torStatus) ->
                if (useTor && !useOrbot) {
                    if (torStatus == TorStatus.OFF || torStatus == TorStatus.IDLE) {
//                        if (!isSocks5Listening(SOCKS5_PROXY_HOSTNAME, SOCKS5_PROXY_PORT)) {
                        start(startForeground = true)
//                        }
                    }
                } else if (!useTor) {
                    if (torStatus == TorStatus.ON) {
                        stop()
                    }
                }

                if ((useTor && !useOrbot && torStatus == TorStatus.ON) || (!useTor && useOrbot)) {
                    queryTorConnection()
                }
            }
        }
    }

    private suspend fun queryTorConnection() {
        _torConnectionCheck.value =
            if (repository.remote.checkTor(CHECK_TOR_URL)?.isTor ?: false) {
                TorConnectionCheck.OK
            } else {
                TorConnectionCheck.FAILED
            }
    }

    fun start(startForeground: Boolean = true) {
        val currentStatus = torServiceMonitor.torStatus.value
        if (currentStatus == TorStatus.ON || currentStatus == TorStatus.STARTING) {
            return
        }

        val intent = Intent(appContext, TorServiceImpl::class.java)
        intent.action = "ON"
        intent.putExtra(START_FOREGROUND_INTENT_EXTRA, startForeground)
        appContext.startService(intent)
    }

    fun stop() {
        val intent = Intent(appContext, TorServiceImpl::class.java)
        intent.action = "OFF"
        appContext.stopService(intent)
    }
}
