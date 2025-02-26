package ro.aenigma.data

import ro.aenigma.crypto.OnionParsingService
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.Message
import ro.aenigma.models.OnionDetails
import ro.aenigma.models.PendingMessage
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.util.NotificationService
import com.google.gson.Gson
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageSaver @Inject constructor(
    private val repository: Repository,
    private val onionParsingService: OnionParsingService,
    private val notificationService: NotificationService
)
{
    private suspend fun deserialize(message: Message): MessageEntity?
    {
        return try {
            val parsedContent = Gson().fromJson(message.text, OnionDetails::class.java)
            createOrUpdateContact(parsedContent)
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

    private suspend fun createOrUpdateContact(onionDetails: OnionDetails)
    {
        val contact = repository.local.getContact(onionDetails.address) ?: ContactEntity(
            onionDetails.address,
            "unknown",
            onionDetails.publicKey,
            onionDetails.guardHostname,
            onionDetails.guardAddress,
            false,
            ZonedDateTime.now()
        )
        contact.guardAddress = onionDetails.guardAddress
        contact.guardHostname = onionDetails.guardHostname
        contact.lastSynchronized = ZonedDateTime.now()

        repository.local.insertOrUpdateContact(contact)
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
