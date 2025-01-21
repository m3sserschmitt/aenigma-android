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
import ro.aenigma.util.MessageType
import ro.aenigma.util.getDescription
import ro.aenigma.util.parseEnum
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageSaver @Inject constructor(
    private val repository: Repository,
    private val onionParsingService: OnionParsingService,
    private val notificationService: NotificationService
) {
    private fun deserializeContent(message: Message): OnionDetails? {
        return try {
            Gson().fromJson(message.text, OnionDetails::class.java)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun execute(data: MessageEntity) {
        try {
            if (data.uuid == null || repository.local.getMessageByUuid(data.uuid) != null) {
                return
            }

            when (data.type) {
                MessageType.DELETE -> {
                    data.refId?.let { repository.local.removeMessageSoft(it) }
                }

                MessageType.DELETE_ALL -> {
                    data.chatId.let { repository.local.clearConversationSoft(it) }
                }

                MessageType.TEXT -> {}
            }
            if(repository.local.insertMessage(data))
            {
                notify(data)
            }
        } catch (_: Exception) {
            return
        }
    }

    private suspend fun interpret(message: Message): MessageEntity? {
        return try {
            val parsedContent = deserializeContent(message) ?: return null
            val action = parsedContent.action.parseEnum<MessageType>()
            if (action == null && parsedContent.text == null) {
                return null
            }
            createOrUpdateContact(parsedContent)
            val text = parsedContent.text ?: action.getDescription() ?: return null
            MessageEntity(
                chatId = message.chatId,
                text = text,
                incoming = true,
                uuid = message.uuid,
                sent = false,
                dateReceivedOnServer = message.dateReceivedOnServer,
                refId = parsedContent.refId,
                type = action ?: MessageType.TEXT
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun handleRoutingRequest(routingRequest: RoutingRequest) {
        val parsedMessage = onionParsingService.parse(routingRequest) ?: return
        val messageEntity = interpret(parsedMessage) ?: return
        saveIncomingMessages(listOf(messageEntity))
    }

    suspend fun handlePendingMessages(messages: List<PendingMessage>) {
        val messageEntities = messages
            .mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item -> interpret(item) }
        saveIncomingMessages(messageEntities)
    }

    suspend fun saveOutgoingMessage(message: MessageEntity) {
        repository.local.insertMessage(message)
    }

    private suspend fun createOrUpdateContact(onionDetails: OnionDetails) {
        if (onionDetails.address == null || onionDetails.publicKey == null || onionDetails.guardAddress == null) {
            return
        }

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

    private suspend fun saveIncomingMessages(messages: List<MessageEntity>) {
        messages.forEach { item -> execute(item) }
    }

    private suspend fun notify(message: MessageEntity) {
        val contact = repository.local.getContact(message.chatId) ?: return
        notificationService.notify(contact, message)
    }
}
