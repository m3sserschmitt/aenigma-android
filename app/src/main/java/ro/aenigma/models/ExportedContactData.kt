package ro.aenigma.models

import java.time.ZonedDateTime

data class ExportedContactData(
    val guardHostname: String? = null,
    val guardAddress: String? = null,
    val publicKey: String? = null,
    val name: String? = null,
    val address: String? = null,
    val dateTimeCreated: ZonedDateTime = ZonedDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExportedContactData) return false
        return address == other.address
    }

    override fun hashCode(): Int {
        return address?.hashCode() ?: 0
    }
}
