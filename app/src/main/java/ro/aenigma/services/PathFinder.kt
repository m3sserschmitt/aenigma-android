package ro.aenigma.services

import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.VertexEntity
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.database.factories.VertexEntityFactory
import javax.inject.Inject

class PathFinder @Inject constructor(
    private val repository: Repository,
    signatureService: SignatureService
) {
    private val _localAddress = signatureService.address

    private val _localPublicKey = signatureService.publicKey

    private var _graph: Graph<VertexEntity, DefaultEdge>? = null

    private var _verticesMap: HashMap<String, VertexEntity>? = null

    private var _localVertex: VertexEntity? = null

    companion object {
        const val MAX_PATH_LENGTH = 6
    }

    suspend fun load(): Boolean {
        return try {
            _graph = DefaultDirectedGraph(DefaultEdge::class.java)
            _localAddress ?: return false
            _localPublicKey ?: return false
            _localVertex = VertexEntityFactory.create(_localAddress, _localPublicKey, null)
            val guard = repository.local.getGuard() ?: return false
            val vertices = repository.local.getVertices()
            _verticesMap = HashMap<String, VertexEntity>(vertices.size)
            val edges = repository.local.getEdges()

            vertices.forEach { vertex ->
                _graph?.addVertex(vertex)
                _verticesMap?.put(vertex.address, vertex)
            }
            val guardVertex = _verticesMap?.get(guard.address) ?: return false

            _graph?.addVertex(_localVertex)
            _verticesMap?.put(_localAddress, _localVertex!!)

            edges.forEach { edge ->
                _verticesMap?.get(edge.sourceAddress)?.let { source ->
                    _verticesMap?.get(edge.targetAddress)?.let { target ->
                        _graph?.addEdge(source, target)
                    }
                }
            }

            _graph?.addEdge(_localVertex, guardVertex)

            true
        } catch (_: Exception) {
            false
        }
    }

    fun calculatePaths(destination: ContactEntity): List<GraphPath<VertexEntity, DefaultEdge>> {
        _graph ?: return listOf()
        destination.publicKey ?: return listOf()
        return try {
            val destinationVertex = VertexEntityFactory.create(destination.address, destination.publicKey, null)
            val destinationGuardVertex = _verticesMap?.get(destination.guardAddress) ?: return listOf()
            _graph?.addVertex(destinationVertex)
            _graph?.addEdge(destinationGuardVertex, destinationVertex)
            AllDirectedPaths(_graph).getAllPaths(_localVertex ?: return listOf(), destinationVertex, true, MAX_PATH_LENGTH)
        } catch (_: Exception) {
            listOf()
        }
    }
}
