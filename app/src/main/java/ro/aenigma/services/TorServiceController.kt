package ro.aenigma.services

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ro.aenigma.data.Repository
import ro.aenigma.util.Constants.Companion.CHECK_TOR_URL
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_ADDRESS
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_PORT
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorServiceController @Inject constructor(
    private val torServiceManager: TorServiceManager,
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

    private val _isTorOk = MutableStateFlow(false)

    val isTorOk: StateFlow<Boolean> = _isTorOk

    fun observeTorService(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            combine(
                repository.local.useTor,
                torServiceManager.torStatus
            ) { useTor, torStatus ->
                Pair(useTor, torStatus)
            }.distinctUntilChanged().collect { (useTor, torStatus) ->
                val proxyIsListening = isSocks5Listening(SOCKS5_PROXY_ADDRESS, SOCKS5_PROXY_PORT)
                when {
                    useTor && !proxyIsListening && (torStatus is TorStatus.Off || torStatus is TorStatus.Idle) -> {
                        torServiceManager.start(startForeground = true)
                    }

                    !useTor && proxyIsListening && torStatus is TorStatus.On -> {
                        torServiceManager.stop()
                    }

                    useTor && proxyIsListening && torStatus is TorStatus.On -> {
                        checkTor()
                    }

                    else -> {
                        _isTorOk.value = false
                    }
                }
            }
        }
    }

    private suspend fun checkTor() {
        withContext(Dispatchers.IO) {
            _isTorOk.value = repository.remote.checkTor(CHECK_TOR_URL)?.isTor ?: false
        }
    }
}
