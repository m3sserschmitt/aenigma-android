package ro.aenigma.services

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import ro.aenigma.models.enums.TorStatus
import ro.aenigma.workers.SignalRWorkerAction
import ro.aenigma.workers.extensions.WorkManagerExtensions.invokeClient
import ro.aenigma.workers.extensions.WorkManagerExtensions.syncGraphAndInvokeClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalrController @Inject constructor(
    private val workManager: WorkManager,
    private val onionRoutingServiceMonitor: OnionRoutingServiceMonitor,
    private val signalRClient: SignalRClient
) {
    val clientStatus = signalRClient.status

    fun enqueueSyncGraphAndReconnect() {
        workManager.syncGraphAndInvokeClient(
            actions = SignalRWorkerAction.Disconnect() and SignalRWorkerAction.connectPullCleanup()
        )
    }

    fun enqueueDisconnect() {
        workManager.invokeClient(
            actions = SignalRWorkerAction.Disconnect()
        )
    }

    private fun enqueue(clientStatus: SignalRStatus) {
        when (clientStatus) {
            is SignalRStatus.Error.Aborted -> {
            }

            SignalRStatus.NotConnected,
            is SignalRStatus.Error.ConnectionRefused,
            is SignalRStatus.Error.Disconnected,
            is SignalRStatus.Error -> {
                enqueueSyncGraphAndReconnect()
            }

            SignalRStatus.Authenticated,
            SignalRStatus.Authenticating,
            SignalRStatus.Clean,
            SignalRStatus.Cleaning,
            SignalRStatus.Connected,
            SignalRStatus.Connecting,
            SignalRStatus.Pulling,
            SignalRStatus.Synchronized -> {
            }
        }
    }

    fun observeSignalrConnection(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            signalRClient.status.collect { clientStatus ->
                enqueue(clientStatus)
            }
        }
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            onionRoutingServiceMonitor.torStatus.drop(1).distinctUntilChanged().collect { status ->
                if (status == TorStatus.ON || status == TorStatus.OFF) {
                    enqueueDisconnect()
                }
            }
        }
    }

    fun resetClient() {
        return signalRClient.reset()
    }

    suspend fun sendMessages(messages: List<String>): Boolean {
        return signalRClient.sendChunkedMessages(messages)
    }

    fun isConnected(): Boolean {
        return signalRClient.isConnected()
    }

    fun isAuthenticated(): Boolean {
        return signalRClient.isAuthenticated()
    }

    suspend fun pull(): Boolean {
        return signalRClient.pull()
    }

    suspend fun connect(host: String?): Boolean {
        return signalRClient.connect(host)
    }

    suspend fun disconnect(): Boolean {
        return signalRClient.disconnect()
    }

    suspend fun cleanup(): Boolean {
        return signalRClient.cleanup()
    }
}
