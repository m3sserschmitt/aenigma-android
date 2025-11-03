package ro.aenigma.data.database.factories

import ro.aenigma.data.database.GuardEntity
import java.time.ZonedDateTime

object GuardEntityFactory {
    @JvmStatic
    fun create(address: String, publicKey: String, graphVersion: String?, hostname: String?, onionService: String?): GuardEntity {
        return GuardEntity(
            address = address,
            publicKey = publicKey,
            hostname = hostname,
            onionService = onionService,
            graphVersion = graphVersion,
            dateCreated = ZonedDateTime.now()
        )
    }
}
