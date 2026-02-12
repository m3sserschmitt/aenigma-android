package ro.aenigma.models.extensions

import ro.aenigma.models.Neighborhood
import ro.aenigma.util.StringExtensions.getHttpUri

object NeighborhoodExtensions {
    @JvmStatic
    fun Neighborhood.normalizeHostname(): Neighborhood {
        return copy(
            hostname = hostname?.getHttpUri(),
            onionService = onionService?.getHttpUri()
        )
    }
}
