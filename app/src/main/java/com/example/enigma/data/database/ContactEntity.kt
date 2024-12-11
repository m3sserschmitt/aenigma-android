package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants.Companion.CONTACTS_TABLE
import java.time.ZonedDateTime

@Entity(tableName = CONTACTS_TABLE)
data class ContactEntity(
    @PrimaryKey val address: String,
    var name: String,
    var publicKey: String,
    var guardHostname: String?,
    var guardAddress: String,
    var hasNewMessage: Boolean,
    var lastSynchronized: ZonedDateTime
) {
    var lastMessageId: Long? = null

    fun toContactWithPreview(): ContactWithConversationPreview
    {
        return ContactWithConversationPreview(
            address,
            name,
            publicKey,
            guardHostname,
            guardAddress,
            hasNewMessage,
            lastSynchronized,
            lastMessageId
        )
    }
}
