package ro.aenigma.util

import ro.aenigma.models.Vertex

open class GraphRequestResult(val graph: List<Vertex>?, val guardSelectionResult: GuardSelectionResult)
{
    class Error: GraphRequestResult(null, GuardSelectionResult.NoGuardAvailable())

    class Success(graph: List<Vertex>, guard: GuardSelectionResult): GraphRequestResult(graph, guard)
}