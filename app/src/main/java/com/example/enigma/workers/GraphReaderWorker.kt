package com.example.enigma.workers

import android.content.Context
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
import kotlinx.coroutines.launch
import java.util.Date

@HiltWorker
class GraphReaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository
) : Worker(context, params)
{

    private suspend fun selectGuard(graph: List<Vertex>)
    {
        // TODO: refactor this to not chose the first item everytime
        val guard = graph[0]

        repository.local.insertGuard(
            GuardEntity(
                guard.neighborhood.address,
                guard.publicKey,
                guard.neighborhood.hostname,
                Date()
            )
        )
    }

    private suspend fun saveGraph(graph: List<Vertex>)
    {
        repository.local.removeVertices()

        val vertices = graph.map {
            VertexEntity(it.neighborhood.address, it.publicKey, it.neighborhood.hostname)
        }

        val edges = graph.map {
                vertex -> vertex.neighborhood.neighbors.map {
            EdgeEntity(vertex.neighborhood.address, it)
                }
        }

        repository.local.insertVertices(vertices)

        for (item in edges)
        {
            if(item.isEmpty())
            {
                continue
            }

            repository.local.insertEdges(item)
        }
    }

    override fun doWork(): Result {

        CoroutineScope(Dispatchers.IO).launch {
            val response = repository.remote.getNetworkGraph()
            val graph = response.body()

            if(response.code() == 200 && graph != null)
            {
                selectGuard(graph)
                saveGraph(graph)
            }
        }

        return Result.success()
    }
}
