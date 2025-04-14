package ro.aenigma.services

import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.VertexEntity
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import javax.inject.Inject

class PathFinder @Inject constructor(
    private val repository: Repository
) {
    private var graph: Graph<VertexEntity, DefaultEdge>? = null

    private var _guardAddress: String? = null

    private var _vertices: List<VertexEntity>? = null

    companion object {
        const val MAX_PATH_LENGTH = 6
    }

    suspend fun load(): Boolean {
        return try {
            val guard = repository.local.getGuard() ?: return false
            val vertices = repository.local.getVertices()
            val edges = repository.local.getEdges()

            graph = DefaultDirectedGraph(DefaultEdge::class.java)

            vertices.forEach { vertex ->
                graph?.addVertex(vertex)
            }

            edges.forEach { edge ->
                graph?.addEdge(
                    vertices.find { item -> item.address == edge.sourceAddress },
                    vertices.find { item -> item.address == edge.targetAddress }
                )
            }

            _guardAddress = guard.address
            _vertices = vertices

            true
        } catch (_: Exception) {
            false
        }
    }

    fun calculatePaths(destination: ContactEntity): List<GraphPath<VertexEntity, DefaultEdge>> {
        if (graph == null) {
            return listOf()
        }

        return try {
            val s = _vertices?.find { item -> item.address == _guardAddress } ?: return listOf()
            val d = _vertices?.find {
                item -> item.address == destination.guardAddress || item.hostname == destination.guardHostname
            } ?: return listOf()

            val algorithm = AllDirectedPaths(graph)
            algorithm.getAllPaths(s, d, true, MAX_PATH_LENGTH)
        } catch (_: Exception) {
            listOf()
        }
    }
}
