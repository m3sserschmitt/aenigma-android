package ro.aenigma.models.factories

import ro.aenigma.models.NeighborhoodDto
import ro.aenigma.models.VertexDto

object VertexDtoFactory {
    @JvmStatic
    fun create(
        address: String,
        publicKey: String,
        hostname: String?,
        onionService: String?
    ): VertexDto {
        return VertexDto(
            address = address,
            publicKey = publicKey,
            neighborhood = NeighborhoodDto(
                address = address,
                hostname = hostname,
                onionService = onionService
            )
        )
    }
}

