package ro.aenigma.data.database.factories

import ro.aenigma.data.database.VertexEntity

object VertexEntityFactory {
    @JvmStatic
    fun create(address: String, publicKey: String, hostname: String?): VertexEntity {
        return VertexEntity(
            address = address,
            publicKey = publicKey,
            hostname = hostname
        )
    }
}
