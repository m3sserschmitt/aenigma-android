package com.example.enigma.workers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.enigma.data.Repository
import com.example.enigma.data.database.EdgeEntity
import com.example.enigma.data.database.GuardEntity
import com.example.enigma.data.database.VertexEntity
import com.example.enigma.models.Vertex
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

@HiltWorker
class GraphReaderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository
) : CoroutineWorker(context, params)
{
    companion object {
        const val GRAPH_DATASTORE_PREFERENCES = "Graph"
        const val GRAPH_DATASTORE_VERSION_KEY = "Version"

        @JvmStatic
        fun startOneTimeWorkRequest(context: Context)
        {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<GraphReaderWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    private val Context.graphVersionDataStore: DataStore<Preferences> by preferencesDataStore(
        name = GRAPH_DATASTORE_PREFERENCES
    )

    private val graphVersionKey = stringPreferencesKey(GRAPH_DATASTORE_VERSION_KEY)

    private fun getGraphVersionFromDataStore(): Flow<String> =
        context.graphVersionDataStore.data.map { preferences ->
            preferences[graphVersionKey] ?: ""
        }

    private suspend fun saveGraphVersionIntoDataStore(graphVersion: String)
    {
        context.graphVersionDataStore.edit { preferences ->
            preferences[graphVersionKey] = graphVersion
        }
    }

    private fun selectGuard(graph: List<Vertex>): Vertex {
        // TODO: refactor this to not chose the first item everytime
        val guard = graph[0]

        return guard
    }

    private suspend fun saveGraph(graph: List<Vertex>, guard: Vertex)
    {
        repository.local.insertGuard(
            GuardEntity(
                guard.neighborhood.address,
                guard.publicKey,
                guard.neighborhood.hostname!!,
                Date()
            )
        )

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

    private suspend fun requestNewGraph()
    {
        val response = repository.remote.getNetworkGraph()
        val graph = response.body()

        if(response.code() == 200 && graph != null)
        {
            val guard = selectGuard(graph)
            saveGraph(graph, guard)
        }
    }

    override suspend fun doWork(): Result {

        val serverInfoResponse = repository.remote.getServerInfo()
        val serverInfo = serverInfoResponse.body() ?: return Result.failure()

        getGraphVersionFromDataStore().collect { graphVersion ->

            if(serverInfoResponse.code() == 200
                && serverInfo.graphVersion != graphVersion)
            {
                clearDatabase()
                requestNewGraph()
            }

            saveGraphVersionIntoDataStore(serverInfo.graphVersion)
        }

        return Result.success()
    }
}
