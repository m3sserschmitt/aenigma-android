/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Singleton
class OnionRoutingServiceMonitor @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val repository: Repository,
    private val notifier: Notifier,
) {
    companion object {
        private const val QUERY_TOR_MAX_ATTEMPTS = 30
        private val QUERY_TOR_DELAY_BETWEEN_ATTEMPTS = 1.5.seconds

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
        suspend fun isProxyActive(
            maxAttempts: Int = QUERY_TOR_MAX_ATTEMPTS,
            delay: Duration = QUERY_TOR_DELAY_BETWEEN_ATTEMPTS
        ): Boolean {
            repeat(maxAttempts) {
                if (isHostListening(
                        Constants.TOR_PROXY_HOSTNAME,
                        Constants.TOR_SOCKS5_PROXY_PORT
                    )
                ) {
                    return true
                }
                delay(delay)
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
                    if (isProxyActive()) {
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

    private suspend fun queryTorConnection(
        maxAttempts: Int = QUERY_TOR_MAX_ATTEMPTS,
        delay: Duration = QUERY_TOR_DELAY_BETWEEN_ATTEMPTS
    ) {
        repeat(maxAttempts) {
            val status = if (repository.remote.checkTor(CHECK_TOR_URL)?.isTor ?: false) {
                TorCircuitState.OK
            } else {
                TorCircuitState.NOT_OK
            }
            _torCircuitState.value = status
            if (status == TorCircuitState.OK) {
                return
            }
            delay(delay)
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
