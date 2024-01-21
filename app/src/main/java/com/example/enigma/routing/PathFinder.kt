package com.example.enigma.routing

import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.GraphPathEntity
import com.example.enigma.data.database.VertexEntity
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import javax.inject.Inject

class PathFinder @Inject constructor(private val repository: Repository) {

    private val graph: Graph<VertexEntity, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)

    private var _guardAddress: String? = null

    private lateinit var _vertices: List<VertexEntity>

    suspend fun load(): Boolean
    {
        val guard = repository.local.getGuard() ?: return false
        val vertices = repository.local.getVertices()
        val edges = repository.local.getEdges()

        vertices.forEach { vertex ->
            graph.addVertex(vertex)
        }

        edges.forEach { edge ->
            graph.addEdge(
                vertices.find { item -> item.address == edge.sourceAddress },
                vertices.find { item -> item.address == edge.targetAddress }
            )
        }

        _guardAddress = guard.address
        _vertices = vertices

        return true
    }

    suspend fun calculatePaths(destination: ContactEntity): Boolean
    {
        val s = _vertices.find { item -> item.address == _guardAddress } ?: return false
        val d = _vertices.find { item -> item.address == destination.guardAddress } ?: return false

        val algorithm = AllDirectedPaths(graph)
        val paths = algorithm.getAllPaths(s, d, true, 6)

        if(paths.isEmpty())
        {
            return false
        }

        for (path in paths)
        {
            val p = listOf(destination.publicKey) + path.vertexList.map { v -> v.publicKey }.reversed()
            repository.local.insertGraphPath(GraphPathEntity(destination.address, p))
        }

        return true
    }
}
