package ro.aenigma.services

import ro.aenigma.data.Repository
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.models.ContactDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.extensions.VertexDtoExtensions.hasHost
import ro.aenigma.models.factories.VertexDtoFactory
import ro.aenigma.util.StringExtensions.getHost
import javax.inject.Inject
import kotlin.collections.get

class PathFinder @Inject constructor(
    private val repository: Repository,
    signatureService: SignatureService
) {
    private val _localAddress = signatureService.address

    private val _localPublicKey = signatureService.publicKey

    private var _graph: Graph<VertexDto, DefaultEdge>? = null

    private var _verticesMap: HashMap<String, VertexDto>? = null

    private var _localVertex: VertexDto? = null

    companion object {
        const val MAX_PATH_LENGTH = 6
    }

    suspend fun load(): Boolean {
        return try {
            _graph = DefaultDirectedGraph(DefaultEdge::class.java)
            _localAddress ?: return false
            _localPublicKey ?: return false
            _localVertex = VertexDtoFactory.create(
                address = _localAddress,
                publicKey = _localPublicKey,
                hostname = null,
                onionService = null
            )
            val guard = repository.local.getGuard() ?: return false
            val vertices = repository.local.getAllVertices()
            _verticesMap = HashMap(vertices.size)
            val edges = repository.local.getEdges()

            vertices.forEach { vertex ->
                vertex.address?.let {
                    _graph?.addVertex(vertex)
                    _verticesMap?.put(vertex.address, vertex)
                }
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

    private fun searchDestination(contact: ContactDto): VertexDto? {
        val destination = _verticesMap?.get(contact.guardAddress)
        val contactGuardHost = contact.guardHostname.getHost()
        if(destination == null && !contactGuardHost.isNullOrBlank()) {
            _verticesMap?.forEach { (_, dto) ->
                if(dto.hasHost(targetHost = contactGuardHost)) {
                    return dto
                }
            }
        }
        return destination
    }

    fun calculatePaths(destination: ContactDto): List<GraphPath<VertexDto, DefaultEdge>> {
        _graph ?: return listOf()
        destination.publicKey ?: return listOf()
        return try {
            val destinationVertex =
                VertexDtoFactory.create(
                    address = destination.address,
                    publicKey = destination.publicKey,
                    hostname = null,
                    onionService = null
                )
            val destinationGuardVertex = searchDestination(destination) ?: return listOf()
            _graph?.addVertex(destinationVertex)
            _graph?.addEdge(destinationGuardVertex, destinationVertex)
            AllDirectedPaths(_graph).getAllPaths(
                _localVertex ?: return listOf(),
                destinationVertex,
                true,
                MAX_PATH_LENGTH
            )
        } catch (_: Exception) {
            listOf()
        }
    }
}
