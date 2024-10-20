package com.example.enigma.data

import com.example.enigma.crypto.OnionParsingService
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.models.OnionDetails
import com.example.enigma.util.NotificationService
import com.google.gson.Gson
import java.time.ZonedDateTime
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
        val messageEntities = messages
            .mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item ->
                try {
                    val gson = Gson()
                    val parsedContent = gson.fromJson(item.text, OnionDetails::class.java)
                    createContact(parsedContent)
                    MessageEntity(item.chatId, parsedContent.text, incoming = true, sent = false, item.date)
                } catch (ex: Exception) {
                    null
                }
            }

        saveMessages(messageEntities)
    }

    suspend fun saveOutgoingMessage(message: MessageEntity)
    {
        saveMessages(listOf(message))
    }

    private suspend fun createContact(messageDetails: OnionDetails)
    {
        val contact = ContactEntity(
            messageDetails.address,
            "unknown",
            messageDetails.publicKey,
            messageDetails.guardHostname,
            messageDetails.guardAddress,
            false,
            ZonedDateTime.now()
        )
        repository.local.insertContact(contact)
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
