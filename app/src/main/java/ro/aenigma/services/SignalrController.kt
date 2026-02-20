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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ro.aenigma.data.Repository
import ro.aenigma.models.enums.TorStatus
import ro.aenigma.workers.GraphReaderWorker
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalrController @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val torServiceManager: TorServiceMonitor,
    private val signalRClient: SignalRClient,
    private val repository: Repository
) {
    val clientStatus = signalRClient.status

    private fun enqueueSignalRWorkRequest() {
        val syncGraphWorkRequest = GraphReaderWorker.createSyncRequest()
        val startConnectionWorkRequest = SignalRClientWorker.createRequest(
            actions = SignalRWorkerAction.connectPullCleanup()
        )
        WorkManager.getInstance(applicationContext).beginWith(syncGraphWorkRequest)
            .then(startConnectionWorkRequest)
            .enqueue()
    }

    private suspend fun performClientAction(clientStatus: SignalRStatus) {
        when (clientStatus) {
            is SignalRStatus.Error.ConnectionRefused,
            is SignalRStatus.Error.Disconnected,
            is SignalRStatus.Reset -> {
                enqueueSignalRWorkRequest()
            }

            is SignalRStatus.Error -> {
                SignalRClientWorker.start(
                    applicationContext,
                    SignalRWorkerAction.Disconnect() and SignalRWorkerAction.connectPullCleanup()
                )
            }

            is SignalRStatus.Synchronized -> { }

            is SignalRStatus.NotConnected -> {
                start()
            }

            else -> {}
        }
    }

    private suspend fun start() {
        val useTor = repository.local.useTor.first()
        if (useTor) {
            torServiceManager.torStatus.filter { status -> status == TorStatus.ON }.first()
                .apply {
                    enqueueSignalRWorkRequest()
                }
        } else {
            enqueueSignalRWorkRequest()
        }
    }

    fun observeSignalrConnection(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            combine(
                repository.local.useTor,
                torServiceManager.torStatus,
                signalRClient.status
            ) { useTor, torStatus, clientStatus ->
                Triple(useTor, torStatus, clientStatus)
            }.distinctUntilChanged().collect { (useTor, torStatus, clientStatus) ->
                if ((useTor && torStatus == TorStatus.ON) || (!useTor)) {
                    performClientAction(clientStatus)
                }
            }
        }
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repository.local.useTor.drop(1).collect {
                signalRClient.disconnect()
            }
        }
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repository.local.useOrbot.drop(1).collect {
                signalRClient.disconnect()
            }
        }
    }

    fun resetClient() {
        return signalRClient.resetAborted()
    }

    fun sendMessages(messages: List<String>): Boolean {
        return signalRClient.sendChunkedMessages(messages)
    }

    fun isConnected(): Boolean {
        return signalRClient.isConnected()
    }

    fun pull() {
        return signalRClient.pull()
    }

    suspend fun connect(host: String) {
        return signalRClient.connect(host)
    }

    fun disconnect() {
        return signalRClient.disconnect()
    }

    fun cleanup() {
        return signalRClient.cleanup()
    }
}
