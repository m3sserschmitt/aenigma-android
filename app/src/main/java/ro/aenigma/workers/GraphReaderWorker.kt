package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ro.aenigma.data.Repository
import ro.aenigma.models.VertexDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.R
import ro.aenigma.models.EdgeDto
import ro.aenigma.models.GuardDto
import ro.aenigma.models.factories.VertexDtoFactory
import ro.aenigma.services.Notifier

@HiltWorker
class GraphReaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {
    companion object {
        private const val MAX_RETRY_COUNT = 3
    }

    private suspend fun saveGraph(graph: List<VertexDto>, guardDto: GuardDto): Boolean {
        if (guardDto.onionService.isNullOrBlank() && guardDto.hostname.isNullOrBlank()) {
            return false
        }
        repository.local.insertGuard(guardDto)

        val vertices = graph.mapNotNull { vertex ->
            vertex.neighborhood?.address?.let { address ->
                vertex.publicKey?.let { publicKey ->
                    VertexDtoFactory.create(
                        address = address,
                        publicKey = publicKey,
                        hostname = vertex.neighborhood.hostname,
                        onionService = vertex.neighborhood.onionService
                    )
                }
            }
        }

        repository.local.insertOrIgnoreVertices(vertices)

        graph.forEach { vertex ->
            vertex.neighborhood?.neighbors?.forEach { neighborAddress ->
                neighborAddress?.let { targetAddress ->
                    vertex.neighborhood.address?.let { sourceAddress ->
                        repository.local.insertOrIgnoreEdge(
                            EdgeDto(
                                sourceAddress = sourceAddress,
                                targetAddress = targetAddress
                            )
                        )
                    }
                }
            }
        }
        return true
    }

    private suspend fun clearDatabase() {
        repository.local.removeVertices()
        repository.local.removeEdges()
    }

    private suspend fun updateLocalGraph(): Boolean {
        try {
            val serverInfo = repository.remote.getServerInfo() ?: return false
            val currentGuard = repository.local.getGuard()

            if (currentGuard == null || serverInfo.graphVersion != currentGuard.graphVersion) {
                val graph = repository.remote.getVertices()
                if (graph.isEmpty()) {
                    return false
                }
                clearDatabase()
                return saveGraph(graph, serverInfo)
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
        return if (!updateLocalGraph()) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.guard_syncing)),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            ) else
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.guard_syncing))
            )
    }
}
