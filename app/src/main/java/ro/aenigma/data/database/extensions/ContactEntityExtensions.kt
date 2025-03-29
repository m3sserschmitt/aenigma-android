package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.ContactEntity
import ro.aenigma.util.SerializerExtensions.deepCopy
import java.time.ZonedDateTime

object ContactEntityExtensions {
    fun ContactEntity?.withLastMessageId(lastMessageId: Long): ContactEntity? {
        return this.deepCopy()?.copy(lastMessageId = lastMessageId)
    }

    fun ContactEntity?.withName(name: String?): ContactEntity? {
        return this.deepCopy()?.copy(name = name, dateUpdated = ZonedDateTime.now())
    }

    fun ContactEntity?.withGuardAddress(guardAddress: String?): ContactEntity? {
        return this.deepCopy()
            ?.copy(guardAddress = guardAddress, dateUpdated = ZonedDateTime.now())
    }

    fun ContactEntity?.withGuardHostname(guardHostname: String?): ContactEntity? {
        return this.deepCopy()
            ?.copy(guardHostname = guardHostname, dateUpdated = ZonedDateTime.now())
    }
}
