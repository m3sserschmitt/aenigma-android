package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.enigma.data.Repository
import com.example.enigma.data.database.EdgeEntity
import com.example.enigma.data.database.GraphVersionEntity
import com.example.enigma.data.database.GuardEntity
import com.example.enigma.data.database.VertexEntity
import com.example.enigma.models.ServerInfo
import com.example.enigma.models.Vertex
import com.example.enigma.util.GraphRequestResult
import com.example.enigma.util.GuardSelectionResult
import com.example.enigma.util.isAppDomain
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Date
import java.util.concurrent.TimeUnit

@HiltWorker
class GraphReaderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository
) : CoroutineWorker(context, params) {
    companion object {

        private const val UNIQUE_REQUEST_IDENTIFIER = "GraphReaderWorkerRequest"

        private const val MAX_RETRY_COUNT = 2

        private const val DELAY_BETWEEN_RETRIES: Long = 3

        @JvmStatic
        fun createSyncRequest() : OneTimeWorkRequest
        {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<GraphReaderWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()
        }

        @JvmStatic
        fun sync(
            context: Context,
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_REQUEST_IDENTIFIER,
                    ExistingWorkPolicy.KEEP,
                    createSyncRequest()
                )
        }
    }

    private fun guardSelectionRequired(graph: List<Vertex>, currentGuard: GuardEntity?): Boolean
    {
        return currentGuard == null || !graph.any { item -> item.neighborhood?.address == currentGuard.address
                && item.publicKey == currentGuard.publicKey
                && item.neighborhood.hostname == currentGuard.hostname
        }
    }

    private suspend fun selectGuard(graph: List<Vertex>): GuardSelectionResult {
        try {
            val currentGuard = repository.local.getGuard()
            if (guardSelectionRequired(graph, currentGuard)) {
                val availableGuards = graph.filter { item -> item.neighborhood != null
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

            val currentGuardVertex = if(currentGuard != null) graph.find {
                item -> item.neighborhood?.address == currentGuard.address
            }
            else
                null
            return if(currentGuardVertex != null)
                GuardSelectionResult.SelectionNotRequired(currentGuardVertex)
            else
                GuardSelectionResult.Error()
        } catch (_: Exception)
        {
            return GuardSelectionResult.Error()
        }
    }

    private suspend fun saveGraph(graphRequestResult: GraphRequestResult)
    {
        if(graphRequestResult is GraphRequestResult.Error) return

        if(graphRequestResult.guardSelectionResult is GuardSelectionResult.Success)
        {
            val chosenGuard = graphRequestResult.guardSelectionResult.chosenGuard!!
            repository.local.insertGuard(
                GuardEntity(
                    chosenGuard.neighborhood!!.address!!,
                    chosenGuard.publicKey!!,
                    chosenGuard.neighborhood.hostname!!,
                    Date()
                )
            )
        }

        val graph = graphRequestResult.graph!!

        val vertices = graph.map { vertex ->
            VertexEntity(vertex.neighborhood?.address ?: "", vertex.publicKey ?: "", vertex.neighborhood?.hostname)
        }

        repository.local.insertVertices(vertices)

        graph.map { vertex ->
            vertex.neighborhood?.neighbors?.map { neighbor ->
                repository.local.insertEdge(EdgeEntity(vertex.neighborhood.address ?: "", neighbor))
            }
        }
    }

    private suspend fun clearDatabase()
    {
        repository.local.removeVertices()
        repository.local.removeEdges()
    }

    private suspend fun requestNewGraph(): GraphRequestResult {
        return try {
            val response = repository.remote.getVertices()
            val vertices = response.body()

            if (response.code() == 200 && vertices != null) {
                val guardSelectionResult = selectGuard(vertices)

                if(guardSelectionResult is GuardSelectionResult.Error)
                    GraphRequestResult.Error()
                else
                    GraphRequestResult.Success(vertices, guardSelectionResult)
            }
            else {
                GraphRequestResult.Error()
            }
        } catch (_: Exception) {
            GraphRequestResult.Error()
        }
    }

    private suspend fun requestServerInfo(): ServerInfo?
    {
        return try {
            val response = repository.remote.getServerInfo()
            val body = response.body()

            if (response.code() == 200 && body != null)
                body
            else
                null
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun updateLocalGraph(
        serverInfo: ServerInfo,
        graphRequestResult: GraphRequestResult): Boolean
    {
        if(serverInfo.graphVersion == null)
        {
            return false
        }
        try {
            val graphVersion = repository.local.getGraphVersion()
            val currentGuard = repository.local.getGuard()

            if (serverInfo.graphVersion != graphVersion?.version || currentGuard == null) {
                clearDatabase()
                saveGraph(graphRequestResult)
            }

            repository.local.updateGraphVersion(GraphVersionEntity(serverInfo.graphVersion, Date()))
        }
        catch (_: Exception)
        {
            return false
        }

        return true
    }

    override suspend fun doWork(): Result {

        val serverInfo = requestServerInfo()
        val graphRequestResult = requestNewGraph()

        if(serverInfo == null ||
            graphRequestResult is GraphRequestResult.Error ||
            !updateLocalGraph(serverInfo, graphRequestResult))
        {
            return if(runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.success()
            }
        }

        return Result.success()
    }
}
