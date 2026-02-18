package ro.aenigma.models.extensions

import ro.aenigma.data.database.GuardEntity
import ro.aenigma.models.GuardDto
import ro.aenigma.models.NeighborhoodDto
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.VertexDto

object GuardDtoExtensions {
    @JvmStatic
    fun GuardDto.toEntity(): GuardEntity {
        return GuardEntity(
            id = id,
            address = address,
            publicKey = publicKey,
            hostname = hostname,
            onionService = onionService,
            graphVersion = graphVersion,
            dateCreated = dateCreated
        )
    }

    @JvmStatic
    fun GuardDto.toVertexDto(): VertexDto {
        return VertexDto(
            address = address,
            publicKey = publicKey,
            signedData = null,
            neighborhood = NeighborhoodDto(
                hostname = hostname,
                onionService = onionService,
                address = address
            )
        )
    }

    @JvmStatic
    fun GuardDto.toServerInfoDto(): ServerInfoDto {
        return ServerInfoDto(
            address = address,
            onionService = onionService,
            hostname = hostname,
            graphVersion = graphVersion
        )
    }

    @JvmStatic
    fun GuardDto.withNoGraphVersion(): GuardDto {
        return copy(graphVersion = null)
    }
}
