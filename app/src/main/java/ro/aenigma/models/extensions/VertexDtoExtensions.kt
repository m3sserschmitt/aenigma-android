package ro.aenigma.models.extensions

import ro.aenigma.data.database.VertexEntity
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.extensions.NeighborhoodExtensions.hasHost

object VertexDtoExtensions {
    @JvmStatic
    fun VertexDto.toEntity(): VertexEntity {
        return VertexEntity(
            publicKey = publicKey ?: "",
            address = neighborhood?.address ?: "",
            hostname = neighborhood?.hostname,
            onionService = neighborhood?.onionService
        )
    }

    @JvmStatic
    fun VertexDto.toServerInfoDto(): ServerInfoDto {
        return ServerInfoDto(
            address = address,
            hostname = neighborhood?.hostname,
            onionService = neighborhood?.onionService,
            graphVersion = null
        )
    }

    @JvmStatic
    fun VertexDto.hasHost(targetHost: String): Boolean {
        return neighborhood?.hasHost(targetHost) == true
    }
}
