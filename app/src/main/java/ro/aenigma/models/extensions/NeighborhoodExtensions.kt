package ro.aenigma.models.extensions

import ro.aenigma.models.NeighborhoodDto
import ro.aenigma.util.StringExtensions.getHost
import ro.aenigma.util.StringExtensions.getHttpUri

object NeighborhoodExtensions {
    @JvmStatic
    fun NeighborhoodDto.normalizeHostname(): NeighborhoodDto {
        return copy(
            hostname = hostname?.getHttpUri(),
            onionService = onionService?.getHttpUri()
        )
    }

    @JvmStatic
    fun NeighborhoodDto.hasHost(targetHost: String): Boolean {
        return hostname.getHost() == targetHost || onionService.getHost() == targetHost
    }
}
