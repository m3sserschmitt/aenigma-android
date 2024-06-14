package com.example.enigma.data

import com.example.enigma.crypto.OnionParsingService
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.models.MessageExtended
import com.example.enigma.util.AddressHelper
import com.example.enigma.util.NotificationService
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageSaver @Inject constructor(
    private val repository: Repository,
    private val onionParsingService: OnionParsingService,
    private val notificationService: NotificationService)
{
    suspend fun handleIncomingMessages(messages: List<String>)
    {
        val parsedData = messages.mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item ->
                try {
                    val gson = Gson()
                    val parsedContent = gson.fromJson(item.text, MessageExtended::class.java)

                    Pair(MessageEntity(item.chatId, parsedContent.text, true, item.date), parsedContent)
                } catch (ex: Exception) {
                    null
                }
            }

        val a = createContacts(parsedData.map { item -> item.second })
        saveMessages(parsedData.map { item -> item.first })
    }

    suspend fun saveOutgoingMessage(message: MessageEntity)
    {
        saveMessages(listOf(message))
    }

    private suspend fun createContacts(contactsInfo: List<MessageExtended>): List<Long>
    {
        val contacts = contactsInfo.mapNotNull { item ->
            try {
                ContactEntity(
                    AddressHelper.getHexAddressFromPublicKey(item.publicKey),
                    "unknown",
                    item.publicKey,
                    item.guardHostname,
                    false
                )
            } catch (_: Exception) {
                null
            }
        }

        return repository.local.insertContacts(contacts)
    }

    private suspend fun saveMessages(messages: List<MessageEntity>)
    {
        messages.map { item -> repository.local.insertMessage(item) }
        markConversationAsUnread(messages)
        notify(messages)
    }

    private suspend fun markConversationAsUnread(messages: List<MessageEntity>)
    {
        val modifiedConversations = messages.map { item -> item.chatId }.toSet()
        modifiedConversations.map { id ->  repository.local.markConversationAsUnread(id)}
    }

    private suspend fun notify(messages: List<MessageEntity>)
    {
        for (message in messages)
        {
            val contact = repository.local.getContact(message.chatId) ?: continue
            notificationService.notify(contact, message.text)
        }
    }
}
