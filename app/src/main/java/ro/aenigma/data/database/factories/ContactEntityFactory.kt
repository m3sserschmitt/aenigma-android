package ro.aenigma.data.database.factories

import ro.aenigma.data.database.ContactEntity
import ro.aenigma.models.enums.ContactType
import java.time.ZonedDateTime

class ContactEntityFactory {
    companion object {
        @JvmStatic
        fun createContact(address: String, name: String?, publicKey: String?, guardHostname: String?,
                   guardAddress: String?
        ): ContactEntity {
            return ContactEntity(
                address = address,
                name = name,
                publicKey = publicKey,
                guardHostname = guardHostname,
                guardAddress = guardAddress,
                type = ContactType.CONTACT,
                hasNewMessage = false,
                lastMessageId = null,
                lastSynchronized = ZonedDateTime.now()
            )
        }

        @JvmStatic
        fun createGroup(address: String, name: String?): ContactEntity {
            return ContactEntity(
                address = address,
                name = name,
                publicKey = null,
                guardHostname = null,
                guardAddress = null,
                type = ContactType.GROUP,
                hasNewMessage = false,
                lastMessageId = null,
                lastSynchronized = ZonedDateTime.now()
            )
        }
    }
}
