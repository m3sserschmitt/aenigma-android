package ro.aenigma.services

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ro.aenigma.data.Repository
import ro.aenigma.workers.CleanupWorker
import ro.aenigma.workers.GraphReaderWorker
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalrConnectionController @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val torServiceManager: TorServiceManager,
    private val signalRClient: SignalRClient,
    private val repository: Repository
) {
    val clientStatus = signalRClient.status

    private fun enqueueSignalRWorkRequest() {
        val syncGraphWorkRequest = GraphReaderWorker.createSyncRequest()
        val startConnectionWorkRequest = SignalRClientWorker.createRequest(
            actions = SignalRWorkerAction.connectPullCleanup() and SignalRWorkerAction.Broadcast()
        )
        WorkManager.getInstance(applicationContext).beginWith(syncGraphWorkRequest)
            .then(startConnectionWorkRequest)
            .enqueue()
    }

    private fun enqueueCleanupWorkRequest() {
        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(cleanupRequest)
    }

    private suspend fun performClientAction(clientStatus: SignalRStatus) {
        when (clientStatus) {
            is SignalRStatus.Error.ConnectionRefused,
            is SignalRStatus.Error.Disconnected,
            is SignalRStatus.Reset -> {
                SignalRClientWorker.start(applicationContext)
            }

            is SignalRStatus.Error -> {
                SignalRClientWorker.start(
                    applicationContext,
                    SignalRWorkerAction.Disconnect() and SignalRWorkerAction.connectPullCleanup()
                )
            }

            is SignalRStatus.Synchronized -> {
                enqueueCleanupWorkRequest()
            }

            is SignalRStatus.NotConnected -> {
                start()
            }

            else -> {}
        }
    }

    private suspend fun start() {
        val useTor = repository.local.useTor.first()
        if (useTor) {
            torServiceManager.torStatus.filter { status -> status is TorStatus.On }.first()
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
                if ((useTor && torStatus == TorStatus.On) || (!useTor)) {
                    performClientAction(clientStatus)
                }
            }
        }
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repository.local.useTor.drop(1).collect { useTor ->
                signalRClient.disconnect()
            }
        }
    }

    val authToken: StateFlow<String> = signalRClient.authToken

    fun resetClient() {
        return signalRClient.resetAborted()
    }

    fun sendMessages(messages: List<String>): Boolean {
        return signalRClient.sendMessages(messages)
    }

    fun isConnected(): Boolean {
        return signalRClient.isConnected()
    }

    fun pull() {
        return signalRClient.pull()
    }

    fun broadcast() {
        return signalRClient.broadcast()
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
