package ro.aenigma.services

import androidx.work.WorkManager
import ro.aenigma.crypto.services.OnionParsingService
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.Artifact
import ro.aenigma.models.PendingMessage
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.extensions.SignatureExtensions.verify
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardAddress
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardHostname
import ro.aenigma.data.database.extensions.MessageEntityExtensions.withSenderAddress
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.SignedData
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

    private suspend fun postToDatabase(data: MessageEntity) {
        try {
            if (data.serverUUID == null || data.refId == null
                || repository.local.getMessageByUuid(data.serverUUID) != null
                || repository.local.getMessageByRefId(data.refId) != null
            ) {
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
                return notify(data)
            }
        } catch (_: Exception) {
            return
        }
    }

    private suspend fun parseArtifact(message: ParsedMessageDto): MessageEntity? {
        return try {
            val signedData = message.content.fromJson<SignedData>() ?: return null
            val artifact = signedData.verify<Artifact>() ?: return null
            createOrUpdateEntities(artifact, signedData.publicKey ?: return null)
            MessageEntityFactory.createIncoming(
                chatId = artifact.chatId ?: return null,
                senderAddress = signedData.publicKey.getAddressFromPublicKey() ?: return null,
                serverUUID = message.uuid,
                text = artifact.text,
                type = artifact.type ?: MessageType.TEXT,
                actionFor = artifact.actionFor,
                refId = artifact.refId,
                dateReceivedOnServer = message.dateReceivedOnServer,
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun handleRoutingRequest(routingRequest: RoutingRequest) {
        val parsedMessage = onionParsingService.parse(routingRequest)
        val messageEntity = parsedMessage.mapNotNull { item -> parseArtifact(item) }
        saveIncomingMessages(messageEntity)
    }

    suspend fun handlePendingMessages(messages: List<PendingMessage>) {
        val messageEntities = messages
            .mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item -> parseArtifact(item) }
        return saveIncomingMessages(messageEntities)
    }

    suspend fun saveOutgoingMessage(
        message: MessageEntity,
        userName: String,
        resourceUrl: String? = null
    ): Long? {
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

    private suspend fun createOrUpdateContact(artifact: Artifact, publicKey: String) {
        val originAddress = publicKey.getAddressFromPublicKey() ?: return
        val contact =
            repository.local.getContact(originAddress) ?: ContactEntityFactory.createContact(
                address = originAddress,
                name = artifact.senderName,
                publicKey = publicKey,
                guardHostname = artifact.senderGuardHostname,
                guardAddress = artifact.senderGuardAddress,
            )
        val updatedContact =
            contact.withGuardAddress(artifact.senderGuardAddress ?: contact.guardAddress)
                .withGuardHostname(artifact.senderGuardHostname ?: contact.guardHostname)
                ?: return
        repository.local.insertOrUpdateContact(updatedContact)
    }

    private suspend fun createOrUpdateGroup(artifact: Artifact) {
        val resourceUrl = artifact.groupResourceUrl ?: return
        val tag = resourceUrl.getTagQueryParameter() ?: return
        val entity = repository.local.getContactWithGroup(artifact.chatId ?: return)
        if (entity?.group?.resourceUrl?.getTagQueryParameter() == tag) {
            return
        }
        GroupDownloadWorker.createWorkRequest(workManager, resourceUrl)
    }

    private suspend fun createOrUpdateEntities(artifact: Artifact, publicKey: String) {
        return if (artifact.groupResourceUrl != null) {
            createOrUpdateGroup(artifact)
        } else {
            createOrUpdateContact(artifact, publicKey)
        }
    }

    private suspend fun saveIncomingMessages(messages: List<MessageEntity>) {
        return messages.forEach { item -> postToDatabase(item) }
    }

    private suspend fun notify(message: MessageEntity) {
        val contact = repository.local.getContact(message.chatId) ?: return
        notificationService.notify(contact, message)
    }
}
