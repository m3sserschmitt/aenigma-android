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
import ro.aenigma.data.database.extensions.GroupEntityExtensions.removeMember
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isDelete
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isGroupUpdate
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isText
import ro.aenigma.data.database.extensions.MessageEntityExtensions.markAsDeleted
import ro.aenigma.data.database.extensions.MessageEntityExtensions.withSenderAddress
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.models.SignedData
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.ArtifactExtensions.toMessage
import ro.aenigma.models.extensions.GroupDataExtensions.iAmAdmin
import ro.aenigma.util.SerializerExtensions.fromJson
import ro.aenigma.util.getTagQueryParameter
import ro.aenigma.workers.GroupDownloadWorker
import ro.aenigma.workers.GroupUploadWorker
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

    private suspend fun saveIncomingMessage(triple: Triple<SignedData, Artifact, MessageEntity>) {
        try {
            val signedData = triple.first
            val artifact = triple.second
            val messageEntity = triple.third
            if (messageEntity.serverUUID == null || messageEntity.refId == null) {
                return
            }

            when {
                messageEntity.type == MessageType.DELETE -> {
                    messageEntity.actionFor?.let { refId ->
                        repository.local.removeMessageSoft(refId)
                    }
                    messageEntity.markAsDeleted()?.let { deletedMessage ->
                        repository.local.insertOrIgnoreMessage(deletedMessage)
                    }
                }

                messageEntity.type == MessageType.DELETE_ALL -> {
                    repository.local.clearConversationSoft(messageEntity.chatId)
                    messageEntity.markAsDeleted()?.let { deletedMessage ->
                        repository.local.insertOrIgnoreMessage(deletedMessage)
                    }
                }

                messageEntity.type == MessageType.GROUP_MEMBER_LEAVE -> {
                    repository.local.getContactWithGroup(messageEntity.chatId)
                        ?.let { contactWithGroup ->
                            if (localAddress != null && contactWithGroup.group?.groupData?.iAmAdmin(
                                    localAddress
                                ) == true
                            ) {
                                GroupUploadWorker.createOrUpdateGroupWorkRequest(
                                    workManager,
                                    groupName = null,
                                    members = listOf(messageEntity.senderAddress ?: return),
                                    existingGroupAddress = messageEntity.chatId,
                                    actionType = MessageType.GROUP_MEMBER_REMOVE
                                )
                            } else {
                                repository.local.insertOrUpdateGroup(
                                    contactWithGroup.group?.removeMember(
                                        messageEntity.senderAddress ?: return
                                    ) ?: return
                                )
                            }
                        }
                }

                messageEntity.isGroupUpdate() -> {
                    repository.local.insertOrIgnoreMessage(messageEntity)?.let { id ->
                        createOrUpdateGroup(artifact, id)
                        notify(messageEntity)
                    }
                }

                messageEntity.isText() -> {
                    repository.local.insertOrIgnoreMessage(messageEntity)
                    createOrUpdateContact(artifact, signedData.publicKey ?: return)
                    notify(messageEntity)
                }
            }
        } catch (_: Exception) {
            return
        }
    }

    private fun parseArtifact(message: ParsedMessageDto): Triple<SignedData, Artifact, MessageEntity>? {
        return try {
            val signedData = message.content.fromJson<SignedData>() ?: return null
            val artifact = signedData.verify<Artifact>() ?: return null
            val message =
                artifact.toMessage(message.uuid ?: return null, message.dateReceivedOnServer)
                    ?: return null
            Triple(signedData, artifact, message)
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
        additionalDestinations: Set<String> = hashSetOf()
    ): Boolean {
        try {
            val entity = message.withSenderAddress(localAddress)?.run {
                if (isDelete()) {
                    markAsDeleted()
                } else {
                    this
                }
            } ?: return false
            val messageId = repository.local.insertOrIgnoreMessage(entity)
            if (messageId != null) {
                MessageSenderWorker.createWorkRequest(
                    workManager,
                    messageId,
                    additionalDestinations
                )
                return true
            }
            return false
        } catch (_: Exception) {
            return false
        }
    }

    private suspend fun createOrUpdateContact(artifact: Artifact, publicKey: String) {
        val originAddress = publicKey.getAddressFromPublicKey() ?: return
        val contact =
            repository.local.getContact(originAddress) ?: ContactEntityFactory.createContact(
                address = artifact.senderAddress ?: return,
                name = artifact.senderName,
                publicKey = publicKey,
                guardHostname = artifact.guardHostname,
                guardAddress = artifact.guardAddress,
            )
        val updatedContact =
            contact.withGuardAddress(artifact.guardAddress ?: contact.guardAddress)
                .withGuardHostname(artifact.guardHostname ?: contact.guardHostname)
                ?: return
        repository.local.insertOrUpdateContact(updatedContact)
    }

    private suspend fun createOrUpdateGroup(artifact: Artifact, messageId: Long) {
        val tag = artifact.resourceUrl?.getTagQueryParameter() ?: return
        val entity = repository.local.getContactWithGroup(artifact.chatId ?: return)
        if (entity?.group?.resourceUrl?.getTagQueryParameter() == tag) {
            return
        }
        GroupDownloadWorker.createWorkRequest(workManager, artifact.resourceUrl, messageId)
    }

    private suspend fun saveIncomingMessages(messages: List<Triple<SignedData, Artifact, MessageEntity>>) {
        return messages.forEach { item -> saveIncomingMessage(item) }
    }

    private suspend fun notify(message: MessageEntity) {
        val contact = repository.local.getContact(message.chatId) ?: return
        notificationService.notify(contact, message)
    }
}
