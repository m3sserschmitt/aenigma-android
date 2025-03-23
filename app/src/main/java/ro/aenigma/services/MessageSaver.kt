package ro.aenigma.services

import androidx.work.WorkManager
import ro.aenigma.crypto.services.OnionParsingService
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.MessageWithMetadata
import ro.aenigma.models.PendingMessage
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.models.MessageAction
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageActionType
import ro.aenigma.util.SerializerExtensions.fromJson
import ro.aenigma.util.getTagQueryParameter
import ro.aenigma.workers.GroupDownloadWorker
import ro.aenigma.workers.MessageSenderWorker
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageSaver @Inject constructor(
    private val repository: Repository,
    private val onionParsingService: OnionParsingService,
    private val notificationService: NotificationService,
    private val workManager: WorkManager,
    signatureService: SignatureService
) {
    private val localAddress = signatureService.address

    private suspend fun execute(data: MessageEntity) {
        try {
            if (data.uuid == null || repository.local.getMessageByUuid(data.uuid) != null) {
                return
            }

            when (data.action.actionType) {
                MessageActionType.DELETE -> {
                    data.action.refId?.let {
                        repository.local.removeMessageSoft(it)
                    }
                }

                MessageActionType.DELETE_ALL -> {
                    data.chatId.let { repository.local.clearConversationSoft(it) }
                }

                else -> {}
            }
            if (repository.local.insertMessage(data) > 0) {
                notify(data)
            }
        } catch (_: Exception) {
            return
        }
    }

    private suspend fun interpret(message: ParsedMessageDto): MessageEntity? {
        return try {
            if(message.chatId == null)
            {
                return null
            }
            val messageWithMetadata = message.content.fromJson<MessageWithMetadata>()
            if (messageWithMetadata?.action == null && messageWithMetadata?.text == null) {
                return null
            }
            createOrUpdateEntities(messageWithMetadata, message.chatId)
            val text = messageWithMetadata.text ?: messageWithMetadata.action?.actionType.toString()
            val action = MessageAction(
                actionType = messageWithMetadata.action?.actionType ?: MessageActionType.TEXT,
                refId = messageWithMetadata.action?.refId,
                senderAddress = messageWithMetadata.senderPublicKey.getAddressFromPublicKey() ?: message.chatId
            )
            MessageEntity(
                chatId = message.chatId,
                text = text,
                incoming = true,
                uuid = message.uuid,
                sent = false,
                dateReceivedOnServer = message.dateReceivedOnServer,
                refId = messageWithMetadata.refId,
                action = action
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun handleRoutingRequest(routingRequest: RoutingRequest) {
        val parsedMessage = onionParsingService.parse(routingRequest)
        val messageEntity = parsedMessage.mapNotNull { item -> interpret(item) }
        saveIncomingMessages(messageEntity)
    }

    suspend fun handlePendingMessages(messages: List<PendingMessage>) {
        val messageEntities = messages
            .mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item -> interpret(item) }
        saveIncomingMessages(messageEntities)
    }

    suspend fun saveOutgoingMessage(message: MessageEntity, userName: String, resourceUrl: String? = null): Long? {
        try {
            message.action.senderAddress = localAddress!!
            val messageId = repository.local.insertMessage(message)
            if (messageId > 0) {
                MessageSenderWorker.createWorkRequest(workManager, messageId, userName, resourceUrl)
            }
            return messageId
        } catch (_: Exception) {
            return null
        }
    }

    private suspend fun createOrUpdateContact(onionDetails: MessageWithMetadata) {
        if (onionDetails.senderPublicKey == null) {
            return
        }
        val originAddress = onionDetails.senderPublicKey.getAddressFromPublicKey() ?: return
        val contact = repository.local.getContact(originAddress) ?: ContactEntity(
            address = originAddress,
            name = onionDetails.senderName ?: return,
            publicKey = onionDetails.senderPublicKey,
            guardHostname = onionDetails.senderGuardHostname,
            guardAddress = onionDetails.senderGuardAddress ?: return,
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )
        contact.guardAddress = onionDetails.senderGuardAddress ?: contact.guardAddress
        contact.guardHostname = onionDetails.senderGuardHostname ?: contact.guardHostname
        contact.lastSynchronized = ZonedDateTime.now()

        repository.local.insertOrUpdateContact(contact)
    }

    private suspend fun createOrUpdateGroup(onionDetails: MessageWithMetadata, chatId: String) {
        val resourceUrl = onionDetails.groupResourceUrl ?: return
        val tag = resourceUrl.getTagQueryParameter() ?: return
        val entity = repository.local.getContactWithGroup(chatId)
        if (entity?.group?.resourceUrl?.getTagQueryParameter() == tag) {
            return
        }
        GroupDownloadWorker.createWorkRequest(workManager, resourceUrl)
    }

    private suspend fun createOrUpdateEntities(onionDetails: MessageWithMetadata, chatId: String) {
        return if (onionDetails.groupResourceUrl != null) {
            createOrUpdateGroup(onionDetails, chatId)
        } else {
            createOrUpdateContact(onionDetails)
        }
    }

    private suspend fun saveIncomingMessages(messages: List<MessageEntity>) {
        messages.forEach { item -> execute(item) }
    }

    private suspend fun notify(message: MessageEntity) {
        val contact = repository.local.getContact(message.chatId) ?: return
        notificationService.notify(contact, message)
    }
}
