package com.example.enigma.routing

import androidx.lifecycle.MutableLiveData
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.GraphPathEntity
import com.example.enigma.data.database.VertexEntity
import kotlinx.coroutines.flow.combine
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import javax.inject.Inject

class PathFinder @Inject constructor(private val repository: Repository) {

    private val graph: Graph<VertexEntity, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)

    val loaded: MutableLiveData<Boolean> = MutableLiveData(false)

    private lateinit var guardAddress: String

    private lateinit var vertices: List<VertexEntity>

    suspend fun load()
    {
        repository.local.getGuard()
            .combine(repository.local.getVertices()) { guard, vertices ->
                Pair(guard, vertices)
            }.combine(repository.local.getEdges()) { (guard, vertices), edges ->
                Triple(guard, vertices, edges)
            }.collect { (guard, v, e) ->

                v.forEach { vertex ->
                    graph.addVertex(vertex)
                }

                e.forEach { edge ->
                    graph.addEdge(
                        vertices.find { item -> item.address == edge.sourceAddress },
                        vertices.find { item -> item.address == edge.targetAddress }
                    )
                }

                guardAddress = guard.address
                vertices = v

                loaded.postValue(true)
            }
    }

    suspend fun calculatePaths(destination: ContactEntity)
    {
        val algorithm = AllDirectedPaths(graph)
        val s = vertices.find { item -> item.address == guardAddress }
        val d = vertices.find { item -> item.address == destination.guardAddress }

        val paths = algorithm.getAllPaths(s, d, true, 6)

        for (path in paths)
        {
            val p = listOf(destination.publicKey) + path.vertexList.map { v -> v.publicKey }.reversed()
            repository.local.insertGraphPath(GraphPathEntity(destination.address, p))
        }
    }
}
