package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.GuardEntity
import ro.aenigma.models.GuardDto

object GuardEntityExtensions {
    @JvmStatic
    fun GuardEntity.toDto(): GuardDto {
        return GuardDto(
            id = id,
            address = address,
            publicKey = publicKey,
            hostname = hostname,
            onionService = onionService,
            graphVersion = graphVersion,
            dateCreated = dateCreated
        )
    }
}
