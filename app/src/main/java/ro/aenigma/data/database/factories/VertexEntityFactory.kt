package ro.aenigma.data.database.factories

import ro.aenigma.data.database.VertexEntity

class VertexEntityFactory {
    companion object {
        @JvmStatic
        fun create(address: String, publicKey: String, hostname: String?): VertexEntity {
            return VertexEntity(
                address = address,
                publicKey = publicKey,
                hostname = hostname
            )
        }
    }
}
