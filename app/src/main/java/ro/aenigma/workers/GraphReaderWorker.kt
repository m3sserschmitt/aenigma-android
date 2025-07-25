package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import ro.aenigma.data.Repository
import ro.aenigma.data.database.GuardEntity
import ro.aenigma.models.ServerInfo
import ro.aenigma.models.Vertex
import ro.aenigma.util.GraphRequestResult
import ro.aenigma.util.GuardSelectionResult
import ro.aenigma.util.isAppDomain
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import ro.aenigma.R
import ro.aenigma.data.database.factories.EdgeEntityFactory
import ro.aenigma.data.database.factories.GuardEntityFactory
import ro.aenigma.data.database.factories.VertexEntityFactory
import ro.aenigma.services.NotificationService
import ro.aenigma.util.Constants.Companion.GRAPH_READER_NOTIFICATION_ID
import java.util.concurrent.TimeUnit

@HiltWorker
class GraphReaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, params) {
    companion object {
        private const val MAX_RETRY_COUNT = 5
        private const val DELAY_BETWEEN_RETRIES: Long = 5

        @JvmStatic
        fun createSyncRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<GraphReaderWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()
        }
    }

    private fun guardSelectionRequired(graph: List<Vertex>, currentGuard: GuardEntity?): Boolean {
        return currentGuard == null || !graph.any { item ->
            item.neighborhood?.address == currentGuard.address
                    && item.publicKey == currentGuard.publicKey
                    && item.neighborhood.hostname == currentGuard.hostname
        }
    }

    private suspend fun selectGuard(graph: List<Vertex>): GuardSelectionResult {
        try {
            val currentGuard = repository.local.getGuard()
            if (guardSelectionRequired(graph, currentGuard)) {
                val availableGuards = graph.filter { item ->
                    item.neighborhood != null
                            && item.neighborhood.hostname.isAppDomain()
                            && !item.neighborhood.address.isNullOrBlank()
                            && !item.publicKey.isNullOrBlank()
                            && !item.signedData.isNullOrBlank()
                }

                return try {
                    /* TODO: this selection mechanism is quite simplistic and might become
                    * inappropriate in the future !! Further refinement will be required;
                    */
                    GuardSelectionResult.Success(availableGuards.random())
                } catch (_: NoSuchElementException) {
                    GuardSelectionResult.NoGuardAvailable()
                }
            }

            val currentGuardVertex = if (currentGuard != null) graph.find { item ->
                item.neighborhood?.address == currentGuard.address
            }
            else
                null
            return if (currentGuardVertex != null)
                GuardSelectionResult.SelectionNotRequired(currentGuardVertex)
            else
                GuardSelectionResult.Error()
        } catch (_: Exception) {
            return GuardSelectionResult.Error()
        }
    }

    private suspend fun saveGraph(graphRequestResult: GraphRequestResult) {
        if (graphRequestResult is GraphRequestResult.Error) return

        if (graphRequestResult.guardSelectionResult is GuardSelectionResult.Success) {
            val chosenGuard = graphRequestResult.guardSelectionResult.chosenGuard ?: return
            chosenGuard.neighborhood?.address ?: return
            chosenGuard.publicKey ?: return
            chosenGuard.neighborhood.hostname ?: return
            repository.local.insertGuard(
                GuardEntityFactory.create(
                    address = chosenGuard.neighborhood.address,
                    publicKey = chosenGuard.publicKey,
                    hostname = chosenGuard.neighborhood.hostname,
                )
            )
        }

        val graph = graphRequestResult.graph ?: return

        val vertices = graph.mapNotNull { vertex ->
            vertex.neighborhood?.address?.let { address ->
                vertex.publicKey?.let { publicKey ->
                    VertexEntityFactory.create(
                        address = address,
                        publicKey = publicKey,
                        hostname = vertex.neighborhood.hostname
                    )
                }
            }
        }

        repository.local.insertOrIgnoreVertices(vertices)

        graph.forEach { vertex ->
            vertex.neighborhood?.neighbors?.map { neighborAddress ->
                neighborAddress?.let { targetAddress ->
                    vertex.neighborhood.address?.let { sourceAddress ->
                        repository.local.insertOrIgnoreEdge(
                            EdgeEntityFactory.create(
                                sourceAddress = sourceAddress,
                                targetAddress = targetAddress
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun clearDatabase() {
        repository.local.removeVertices()
        repository.local.removeEdges()
    }

    private suspend fun requestNewGraph(): GraphRequestResult {
        return try {
            val vertices = repository.remote.getVertices()
            if (vertices.isNotEmpty()) {
                val guardSelectionResult = selectGuard(vertices)

                if (guardSelectionResult is GuardSelectionResult.Error)
                    GraphRequestResult.Error()
                else
                    GraphRequestResult.Success(vertices, guardSelectionResult)
            } else {
                GraphRequestResult.Error()
            }
        } catch (_: Exception) {
            GraphRequestResult.Error()
        }
    }

    private suspend fun updateLocalGraph(serverInfo: ServerInfo): Boolean {
        try {
            val previousGraphVersion = repository.local.lastGraphVersion.first()
            val currentGuard = repository.local.getGuard()

            if (serverInfo.graphVersion != previousGraphVersion || currentGuard == null) {
                val graphRequestResult = requestNewGraph()
                clearDatabase()
                saveGraph(graphRequestResult)
            }
            if(serverInfo.graphVersion != null)
            {
                repository.local.saveLastGraphVersion(serverInfo.graphVersion)
            }
        } catch (_: Exception) {
            return false
        }

        return true
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }
        val serverInfo = repository.remote.getServerInfo()

        return if (serverInfo == null || !updateLocalGraph(serverInfo)) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(
                GRAPH_READER_NOTIFICATION_ID,
                notificationService.createWorkerNotification(applicationContext.getString(R.string.guard_syncing)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                GRAPH_READER_NOTIFICATION_ID,
                notificationService.createWorkerNotification(applicationContext.getString(R.string.guard_syncing))
            )
    }
}
