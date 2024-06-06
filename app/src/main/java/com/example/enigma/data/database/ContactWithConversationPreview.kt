package com.example.enigma.data.database

data class ContactWithConversationPreview (
    val address: String,
    var name: String,
    val publicKey: String,
    val guardHostname: String,
    val hasNewMessage: Boolean,
    val lastMessageId: Long? = null,
    val lastMessageText: String? = null,
    val lastMessageIncoming: Boolean? = null
) {
    fun toContact(): ContactEntity {
        val contact = ContactEntity(address, name, publicKey, guardHostname, hasNewMessage)
        contact.lastMessageId = lastMessageId
        return contact
    }
}
