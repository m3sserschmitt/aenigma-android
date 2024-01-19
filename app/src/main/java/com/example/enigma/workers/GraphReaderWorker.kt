package com.example.enigma.workers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.enigma.data.Repository
import com.example.enigma.data.database.EdgeEntity
import com.example.enigma.data.database.GuardEntity
import com.example.enigma.data.database.VertexEntity
import com.example.enigma.models.Vertex
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

@HiltWorker
class GraphReaderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository
) : Worker(context, params)
{
    companion object {
        const val GRAPH_DATASTORE_PREFERENCES = "Graph"
        const val GRAPH_DATASTORE_VERSION_KEY = "Version"
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

        repository.local.removeVertices()
        repository.local.removeEdges()

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

    private suspend fun requestGraph()
    {
        val response = repository.remote.getNetworkGraph()
        val graph = response.body()

        if(response.code() == 200 && graph != null)
        {
            val guard = selectGuard(graph)
            saveGraph(graph, guard)
        }
    }

    override fun doWork(): Result {

        CoroutineScope(Dispatchers.IO).launch {
            val serverInfoResponse = repository.remote.getServerInfo()

            getGraphVersionFromDataStore().collect { graphVersion ->
                val serverInfo = serverInfoResponse.body()

                if(serverInfoResponse.code() == 200
                    && serverInfo != null
                    && serverInfo.graphVersion != graphVersion)
                {
                    requestGraph()
                }

                saveGraphVersionIntoDataStore(serverInfo?.graphVersion ?: "")
            }
        }

        return Result.success()
    }
}
