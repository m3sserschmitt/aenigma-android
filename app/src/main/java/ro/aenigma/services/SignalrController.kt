package ro.aenigma.services

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import ro.aenigma.data.Repository
import ro.aenigma.models.enums.TorStatus
import ro.aenigma.models.extensions.TorStatusExtensions.torPreferenceIsChanging
import ro.aenigma.models.extensions.TorStatusExtensions.with
import ro.aenigma.workers.GraphReaderWorker
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalrController @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val torServiceMonitor: TorServiceMonitor,
    private val signalRClient: SignalRClient,
    private val repository: Repository
) {
    val clientStatus = signalRClient.status

    private fun enqueueSyncAndConnect() {
        val syncGraphWorkRequest = GraphReaderWorker.createSyncRequest()
        val startConnectionWorkRequest = SignalRClientWorker.createRequest(
            actions = SignalRWorkerAction.connectPullCleanup()
        )
        WorkManager.getInstance(applicationContext)
            .beginWith(syncGraphWorkRequest)
            .then(startConnectionWorkRequest)
            .enqueue()
    }

    private fun enqueueReconnect() {
        SignalRClientWorker.start(
            context = applicationContext,
            actions = SignalRWorkerAction.Disconnect() and SignalRWorkerAction.connectPullCleanup()
        )
    }

    private fun enqueue(clientStatus: SignalRStatus, torPreference: Boolean, torStatus: TorStatus) {
        when (clientStatus) {
            SignalRStatus.NotConnected,
            is SignalRStatus.Error.ConnectionRefused -> {
                enqueueSyncAndConnect()
            }

            is SignalRStatus.Error.Disconnected -> {
                enqueueSyncAndConnect()
            }

            is SignalRStatus.Error.Aborted -> {
            }

            is SignalRStatus.Error -> {
                enqueueReconnect()
            }

            SignalRStatus.Authenticated,
            SignalRStatus.Authenticating,
            SignalRStatus.Clean,
            SignalRStatus.Cleaning,
            SignalRStatus.Connected,
            SignalRStatus.Connecting,
            SignalRStatus.Pulling,
            SignalRStatus.Synchronized -> {
                if (torStatus.torPreferenceIsChanging(torPreference)) {
                    enqueueReconnect()
                }
            }
        }
    }

    fun observeSignalrConnection(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            combine(
                repository.local.useTor,
                torServiceMonitor.torStatus,
                signalRClient.status
            ) { torPreference, torStatus, clientStatus ->
                Triple(torPreference, torStatus, clientStatus)
            }.distinctUntilChanged().collect { (torPreference, torStatus, clientStatus) ->
                torStatus.with(torPreference) { enqueue(clientStatus, torPreference, torStatus) }
            }
        }
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repository.local.useOrbot.drop(1).collect { enqueueReconnect() }
        }
    }

    fun resetClient() {
        return signalRClient.resetAborted()
    }

    suspend fun sendMessages(messages: List<String>): Boolean {
        return signalRClient.sendChunkedMessages(messages)
    }

    fun isConnected(): Boolean {
        return signalRClient.isConnected()
    }

    suspend fun pull(): Boolean {
        return signalRClient.pull()
    }

    suspend fun connect(host: String): Boolean {
        return signalRClient.connect(host)
    }

    suspend fun disconnect(): Boolean {
        return signalRClient.disconnect()
    }

    suspend fun cleanup(): Boolean {
        return signalRClient.cleanup()
    }
}
