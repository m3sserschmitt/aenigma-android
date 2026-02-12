package ro.aenigma.models.extensions

import ro.aenigma.data.database.GuardEntity
import ro.aenigma.data.database.factories.GuardEntityFactory
import ro.aenigma.models.GuardDto

object GuardDtoExtensions {
    fun GuardDto.toEntity(): GuardEntity {
        return GuardEntityFactory.create(
            address = address,
            publicKey = publicKey,
            hostname = hostname,
            onionService = onionService,
            graphVersion = graphVersion
        )
    }
}
