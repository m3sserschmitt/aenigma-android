package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
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

        const val FORCE_CHANGE_GUARD_PARAMETER = "force-change-guard"

        private const val UNIQUE_REQUEST_IDENTIFIER = "GraphReaderWorkerRequest"

        private const val MAX_RETRY_COUNT = 5

        private const val DELAY_BETWEEN_RETRIES: Long = 10

        @JvmStatic
        fun sync(
            context: Context,
            forceReplaceGuard: Boolean = false
        ) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val parameters = Data.Builder()
                .putBoolean(FORCE_CHANGE_GUARD_PARAMETER, forceReplaceGuard)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<GraphReaderWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_REQUEST_IDENTIFIER,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
        }

        @JvmStatic
        fun syncReplaceGuard(context: Context)
        {
            sync(context, true)
        }
    }

    private val forceChangeGuard get() = inputData.getBoolean(FORCE_CHANGE_GUARD_PARAMETER, false)

    private fun guardChangeRequired(currentGuard: GuardEntity?, graph: List<Vertex>): Boolean
    {
        return currentGuard == null ||
                !graph.any { item -> item.neighborhood.address == currentGuard.address }
    }

    private suspend fun selectGuard(graph: List<Vertex>): GuardSelectionResult {
        try {
            val currentGuard = repository.local.getGuard()

            if (forceChangeGuard || guardChangeRequired(currentGuard, graph)) {
                val availableGuards = graph.filter { item ->
                    !item.neighborhood.hostname.isNullOrBlank()
                            && currentGuard?.address != item.neighborhood.address
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

            return GuardSelectionResult.SelectionNotRequired()
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
                    chosenGuard.neighborhood.address,
                    chosenGuard.publicKey,
                    chosenGuard.neighborhood.hostname!!,
                    Date()
                )
            )
        }

        val graph = graphRequestResult.graph!!

        val vertices = graph.map { vertex ->
            VertexEntity(vertex.neighborhood.address, vertex.publicKey, vertex.neighborhood.hostname)
        }

        repository.local.insertVertices(vertices)

        graph.map { vertex ->
            vertex.neighborhood.neighbors.map { neighbor ->
                repository.local.insertEdge(EdgeEntity(vertex.neighborhood.address, neighbor))
            }
        }
    }

    private suspend fun clearDatabase()
    {
        repository.local.removeVertices()
        repository.local.removeEdges()
        repository.local.removeGraphPaths()
    }

    private suspend fun requestNewGraph(): GraphRequestResult {
        return try {
            val response = repository.remote.getNetworkGraph()
            val graph = response.body()

            if (response.code() == 200 && graph != null) {

                val guardSelectionResult = selectGuard(graph)

                if(guardSelectionResult is GuardSelectionResult.Error)
                    GraphRequestResult.Error()
                else
                    GraphRequestResult.Success(graph, guardSelectionResult)
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
        try {
            val graphVersion = repository.local.getGraphVersion()
            val currentGuard = repository.local.getGuard()

            if (serverInfo.graphVersion != graphVersion?.version ||
                forceChangeGuard ||
                currentGuard == null
            ) {
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
                Result.failure()
            }
        }

        return Result.success()
    }
}
