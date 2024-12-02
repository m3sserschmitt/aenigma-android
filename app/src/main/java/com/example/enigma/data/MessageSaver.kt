package com.example.enigma.data

import com.example.enigma.crypto.OnionParsingService
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.models.Message
import com.example.enigma.models.OnionDetails
import com.example.enigma.models.PendingMessage
import com.example.enigma.models.hubInvocation.RoutingRequest
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
    private suspend fun deserialize(message: Message): MessageEntity?
    {
        return try {
            val parsedContent = Gson().fromJson(message.text, OnionDetails::class.java)
            createContact(parsedContent)
            MessageEntity(
                message.chatId,
                parsedContent.text,
                incoming = true,
                sent = false,
                uuid = message.uuid,
                dateReceivedOnServer = message.dateReceivedOnServer
            )
        }
        catch (_: Exception)
        {
            null
        }
    }

    suspend fun handleRoutingRequest(routingRequest: RoutingRequest)
    {
        val parsedMessage = onionParsingService.parse(routingRequest) ?: return
        val messageEntity = deserialize(parsedMessage) ?: return
        saveMessages(listOf(messageEntity))
    }

    suspend fun handlePendingMessages(messages: List<PendingMessage>)
    {
        val messageEntities = messages
            .mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item -> deserialize(item) }
            .filter { item -> item.uuid == null || repository.local.getMessage(item.uuid) == null }
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
        repository.local.insertMessages(messages)
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
            notificationService.notify(contact, message)
        }
    }
}
