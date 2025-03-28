package ro.aenigma.services

import androidx.work.WorkManager
import ro.aenigma.crypto.services.OnionParsingService
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.MessageWithMetadata
import ro.aenigma.models.PendingMessage
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardAddress
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardHostname
import ro.aenigma.data.database.extensions.MessageEntityExtensions.withSenderAddress
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.enums.MessageType
import ro.aenigma.util.SerializerExtensions.fromJson
import ro.aenigma.util.getTagQueryParameter
import ro.aenigma.workers.GroupDownloadWorker
import ro.aenigma.workers.MessageSenderWorker
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
            if (data.serverUUID == null || repository.local.getMessageByUuid(data.serverUUID) != null) {
                return
            }

            when (data.type) {
                MessageType.DELETE -> {
                    data.actionFor?.let {
                        repository.local.removeMessageSoft(it)
                    }
                }

                MessageType.DELETE_ALL -> {
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
            val messageWithMetadata = message.content.fromJson<MessageWithMetadata>() ?: return null
            createOrUpdateEntities(messageWithMetadata, message.chatId)
            MessageEntityFactory.createIncoming(
                chatId = message.chatId,
                senderAddress = messageWithMetadata.senderPublicKey.getAddressFromPublicKey() ?: message.chatId,
                serverUUID = message.uuid,
                text = messageWithMetadata.text,
                type = messageWithMetadata.type ?: MessageType.TEXT,
                actionFor = messageWithMetadata.actionFor,
                refId = messageWithMetadata.refId,
                dateReceivedOnServer = message.dateReceivedOnServer,
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
            val entity = message.withSenderAddress(localAddress) ?: return null
            val messageId = repository.local.insertMessage(entity)
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
        val contact =
            repository.local.getContact(originAddress) ?: ContactEntityFactory.createContact(
                address = originAddress,
                name = onionDetails.senderName,
                publicKey = onionDetails.senderPublicKey,
                guardHostname = onionDetails.senderGuardHostname,
                guardAddress = onionDetails.senderGuardAddress,
            )
        val updatedContact =
            contact.withGuardAddress(onionDetails.senderGuardAddress ?: contact.guardAddress)
                .withGuardHostname(onionDetails.senderGuardHostname ?: contact.guardHostname)
                ?: return
        repository.local.insertOrUpdateContact(updatedContact)
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
