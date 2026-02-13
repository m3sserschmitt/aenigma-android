package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.VertexEntity
import ro.aenigma.models.NeighborhoodDto
import ro.aenigma.models.VertexDto

object VertexEntityExtensions {
    @JvmStatic
    fun VertexEntity.toDto(): VertexDto {
        return VertexDto(
            address = address,
            publicKey = publicKey,
            signedData = null,
            neighborhood = NeighborhoodDto(
                address = address,
                hostname = hostname,
                onionService = onionService
            )
        )
    }
}
