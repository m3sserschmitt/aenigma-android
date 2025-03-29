package ro.aenigma.data.database.factories

import ro.aenigma.data.database.GuardEntity
import java.time.ZonedDateTime

class GuardEntityFactory {
    companion object {
        @JvmStatic
        fun create(address: String, publicKey: String, hostname: String): GuardEntity {
            return GuardEntity(
                address = address,
                publicKey = publicKey,
                hostname = hostname,
                dateCreated = ZonedDateTime.now()
            )
        }
    }
}
