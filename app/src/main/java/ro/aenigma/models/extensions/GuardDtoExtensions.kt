package ro.aenigma.models.extensions

import ro.aenigma.data.database.GuardEntity
import ro.aenigma.models.GuardDto
import ro.aenigma.models.ServerInfoDto

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

    @JvmStatic
    fun GuardDto.getHostname(): String? {
        return if(hostname.isNullOrBlank()) {
            onionService
        } else {
            hostname
        }
    }
}
