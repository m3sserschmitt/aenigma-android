package ro.aenigma.services

import androidx.work.WorkManager
import ro.aenigma.crypto.services.OnionParsingService
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.ArtifactDto
import ro.aenigma.models.PendingMessageDto
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.extensions.SignatureExtensions.jsonVerify
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.AttachmentEntity
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardAddress
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardHostname
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withNewMessage
import ro.aenigma.data.database.extensions.GroupEntityExtensions.removeMember
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isFile
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isDelete
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isGroupUpdate
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isText
import ro.aenigma.data.database.extensions.MessageEntityExtensions.markAsDeleted
import ro.aenigma.data.database.extensions.MessageEntityExtensions.withSenderAddress
import ro.aenigma.data.database.factories.AttachmentEntityFactory
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.models.SignatureDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.ArtifactExtensions.toMessage
import ro.aenigma.models.extensions.GroupDataExtensions.iAmAdmin
import ro.aenigma.util.StringExtensions.fromJson
import ro.aenigma.workers.AttachmentDownloadWorker
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

    private suspend fun saveIncomingMessage(triple: Triple<SignatureDto, ArtifactDto, MessageEntity>) {
        try {
            val signedData = triple.first
            val artifact = triple.second
            val messageEntity = triple.third
            if (messageEntity.serverUUID == null || messageEntity.refId == null) {
                return
            }

            when {
                messageEntity.type == MessageType.DELETE -> {
                    messageEntity.markAsDeleted()?.let { deletedMessage ->
                        repository.local.insertOrIgnoreMessage(deletedMessage)?.let {
                            messageEntity.actionFor?.let { refId ->
                                repository.local.removeMessageSoft(refId)
                            }
                        }
                    }
                }

                messageEntity.type == MessageType.DELETE_ALL -> {
                    messageEntity.markAsDeleted()?.let { deletedMessage ->
                        repository.local.insertOrIgnoreMessage(deletedMessage)?.let {
                            repository.local.clearConversationSoft(messageEntity.chatId)
                        }
                    }
                }

                messageEntity.type == MessageType.GROUP_MEMBER_LEAVE -> {
                    repository.local.insertOrIgnoreMessage(messageEntity)?.let {
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
                }

                messageEntity.isGroupUpdate() -> {
                    repository.local.insertOrIgnoreMessage(messageEntity)?.let { id ->
                        downloadGroupData(artifact, id)
                        notify(messageEntity)
                    }
                }

                messageEntity.isText() -> {
                    repository.local.insertOrIgnoreMessage(messageEntity)?.let {
                        createOrUpdateContact(artifact, signedData.publicKey ?: return)
                        notify(messageEntity)
                    }
                }

                messageEntity.isFile() -> {
                    repository.local.insertOrIgnoreMessage(messageEntity)?.let { id ->
                        createOrUpdateContact(artifact, signedData.publicKey ?: return)
                        downloadAttachment(artifact, id)
                        notify(messageEntity)
                    }
                }
            }
        } catch (_: Exception) {
            return
        }
    }

    private fun parseArtifact(message: ParsedMessageDto): Triple<SignatureDto, ArtifactDto, MessageEntity>? {
        return try {
            val signedData = message.content.fromJson<SignatureDto>() ?: return null
            val artifactDto = signedData.jsonVerify<ArtifactDto>() ?: return null
            val message =
                artifactDto.toMessage(message.uuid ?: return null, message.dateReceivedOnServer)
                    ?: return null
            Triple(signedData, artifactDto, message)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun handleRoutingRequest(routingRequest: RoutingRequest) {
        val parsedMessage = onionParsingService.parse(routingRequest)
        val messageEntity = parsedMessage.mapNotNull { item -> parseArtifact(item) }
        saveIncomingMessages(messageEntity)
    }

    suspend fun handlePendingMessages(messages: List<PendingMessageDto>) {
        val messageEntities = messages
            .mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item -> parseArtifact(item) }
        return saveIncomingMessages(messageEntities)
    }

    suspend fun saveOutgoingMessage(
        message: MessageEntity,
        additionalDestinations: Set<String> = hashSetOf(),
        attachment: AttachmentEntity? = null
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
                if (attachment != null) {
                    repository.local.insertOrUpdateAttachment(attachment.copy(messageId = messageId))
                }
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

    suspend fun saveOutgoingMessages(messages: List<MessageEntity>): Boolean {
        return messages.map { message -> saveOutgoingMessage(message) }.all { it }
    }

    private suspend fun createOrUpdateContact(artifactDto: ArtifactDto, publicKey: String) {
        val originAddress = publicKey.getAddressFromPublicKey() ?: return
        val contact =
            repository.local.getContact(originAddress) ?: ContactEntityFactory.createContact(
                address = artifactDto.senderAddress ?: return,
                name = artifactDto.senderName,
                publicKey = publicKey,
                guardHostname = artifactDto.guardHostname,
                guardAddress = artifactDto.guardAddress,
            )
        val updatedContact =
            contact.withGuardAddress(artifactDto.guardAddress ?: contact.guardAddress)
                .withGuardHostname(artifactDto.guardHostname ?: contact.guardHostname)
                .run {
                    if (artifactDto.chatId == artifactDto.senderAddress) {
                        withNewMessage()
                    } else {
                        this
                    }
                } ?: return
        repository.local.insertOrUpdateContact(updatedContact)
    }

    private suspend fun createAttachmentEntity(artifactDto: ArtifactDto, messageId: Long) {
        repository.local.insertOrUpdateAttachment(
            AttachmentEntityFactory.create(
                id = messageId,
                path = null,
                url = artifactDto.resourceUrl,
                passphrase = artifactDto.passphrase
            )
        )
    }

    private suspend fun downloadGroupData(artifactDto: ArtifactDto, messageId: Long) {
        createAttachmentEntity(artifactDto, messageId)
        GroupDownloadWorker.createWorkRequest(workManager, messageId)
    }

    private suspend fun downloadAttachment(artifactDto: ArtifactDto, messageId: Long) {
        createAttachmentEntity(artifactDto, messageId)
        AttachmentDownloadWorker.createRequest(workManager, messageId)
    }

    private suspend fun saveIncomingMessages(messages: List<Triple<SignatureDto, ArtifactDto, MessageEntity>>) {
        return messages.forEach { item -> saveIncomingMessage(item) }
    }

    private suspend fun notify(message: MessageEntity) {
        val contact = repository.local.getContact(message.chatId) ?: return
        notificationService.notifyNewMessage(contact, message)
    }
}
