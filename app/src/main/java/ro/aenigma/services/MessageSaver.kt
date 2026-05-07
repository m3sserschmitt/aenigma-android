package ro.aenigma.services

import androidx.work.WorkManager
import ro.aenigma.crypto.services.OnionParsingService
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.ArtifactDto
import ro.aenigma.models.PendingMessageDto
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.extensions.SignatureExtensions.jsonVerify
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.models.AttachmentDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.SignatureDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.ArtifactDtoExtensions.isGroupCreate
import ro.aenigma.models.extensions.ArtifactDtoExtensions.isGroupUpdate
import ro.aenigma.models.extensions.ArtifactDtoExtensions.toMessageDto
import ro.aenigma.models.extensions.ContactDtoExtensions.withGuardAddress
import ro.aenigma.models.extensions.ContactDtoExtensions.withGuardHostname
import ro.aenigma.models.extensions.ContactDtoExtensions.withNewMessage
import ro.aenigma.models.extensions.GroupDataExtensions.iAmAdmin
import ro.aenigma.models.extensions.GroupDtoExtensions.removeMember
import ro.aenigma.models.extensions.MessageDtoExtensions.isDelete
import ro.aenigma.models.extensions.MessageDtoExtensions.isDeleteOrDeleteAll
import ro.aenigma.models.extensions.MessageDtoExtensions.isDeleteAll
import ro.aenigma.models.extensions.MessageDtoExtensions.isFile
import ro.aenigma.models.extensions.MessageDtoExtensions.isGroupCreateOrUpdate
import ro.aenigma.models.extensions.MessageDtoExtensions.isGroupMemberLeave
import ro.aenigma.models.extensions.MessageDtoExtensions.isText
import ro.aenigma.models.extensions.MessageDtoExtensions.markAsDeleted
import ro.aenigma.models.extensions.MessageDtoExtensions.withSenderAddress
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.util.StringExtensions.fromJson
import ro.aenigma.workers.extensions.WorkManagerExtensions.createOrUpdateGroup
import ro.aenigma.workers.extensions.WorkManagerExtensions.downloadAttachment
import ro.aenigma.workers.extensions.WorkManagerExtensions.downloadGroupData
import ro.aenigma.workers.extensions.WorkManagerExtensions.invokeClient
import ro.aenigma.workers.extensions.WorkManagerExtensions.sendMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageSaver @Inject constructor(
    private val repository: Repository,
    private val onionParsingService: OnionParsingService,
    private val notifier: Notifier,
    private val workManager: WorkManager,
    signatureService: SignatureService
) {
    private val localAddress = signatureService.address

    private suspend fun saveIncomingMessage(triple: Triple<SignatureDto, ArtifactDto, MessageDto>) {
        try {
            val signedData = triple.first
            val artifact = triple.second
            val messageEntity = triple.third
            if (messageEntity.serverUUID == null || messageEntity.refId == null) {
                return
            }

            when {
                messageEntity.isDelete() -> {
                    messageEntity.markAsDeleted().let { deletedMessage ->
                        repository.local.insertOrIgnoreMessage(deletedMessage)?.let {
                            messageEntity.actionFor?.let { refId ->
                                repository.local.removeMessageSoft(refId)
                            }
                        }
                    }
                }

                messageEntity.isDeleteAll() -> {
                    messageEntity.markAsDeleted().let { deletedMessage ->
                        repository.local.insertOrIgnoreMessage(deletedMessage)?.let {
                            repository.local.clearConversationSoft(messageEntity.chatId)
                        }
                    }
                }

                messageEntity.isGroupMemberLeave() -> {
                    repository.local.insertOrIgnoreMessage(messageEntity)?.let {
                        repository.local.getContactWithGroup(messageEntity.chatId)
                            ?.let { contactWithGroup ->
                                if (localAddress != null && contactWithGroup.group?.groupData?.iAmAdmin(
                                        localAddress
                                    ) == true
                                ) {
                                    workManager.createOrUpdateGroup(
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

                messageEntity.isGroupCreateOrUpdate() -> {
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

    private suspend fun parseArtifact(message: ParsedMessageDto): Triple<SignatureDto, ArtifactDto, MessageDto>? {
        return try {
            val signedData = message.content.fromJson<SignatureDto>() ?: return null
            val artifact = verifyArtifact(message.chatId, signedData) ?: return null
            val message =
                artifact.toMessageDto(message.uuid ?: return null, message.dateReceivedOnServer)
                    ?: return null
            Triple(signedData, artifact, message)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun verifyArtifact(chatId: String?, signedData: SignatureDto): ArtifactDto? {
        if (chatId.isNullOrBlank()) {
            return null
        }
        val artifact = signedData.jsonVerify<ArtifactDto>() ?: return null
        val publicKeyMatch =
            signedData.publicKey.getAddressFromPublicKey() == artifact.senderAddress
        return when (publicKeyMatch && chatId == artifact.senderAddress) {
            true -> artifact
            false -> when (publicKeyMatch) {
                true -> {
                    if (artifact.isGroupCreate()) {
                        artifact
                    } else if (artifact.isGroupUpdate()) {
                        val contactWithGroup = repository.local.getContactWithGroup(chatId)
                        if (contactWithGroup == null
                            || contactWithGroup.group?.groupData?.admins?.contains(artifact.senderAddress) == true
                        ) {
                            artifact
                        } else {
                            null
                        }
                    } else {
                        val contactWithGroup = repository.local.getContactWithGroup(chatId)
                        val currentMemberAddresses = contactWithGroup?.group?.groupData?.members
                            ?.mapNotNullTo(mutableSetOf()) { member -> member.address } ?: setOf()
                        if (currentMemberAddresses.contains(artifact.senderAddress)
                            && currentMemberAddresses.contains(localAddress)
                        ) {
                            artifact
                        } else {
                            null
                        }
                    }
                }

                false -> {
                    null
                }
            }
        }
    }

    suspend fun handleRoutingRequest(routingRequest: RoutingRequest) {
        val parsedMessage = onionParsingService.parse(routingRequest)
        val messageEntity = parsedMessage.mapNotNull { item -> parseArtifact(item) }
        saveIncomingMessages(messageEntity)
        workManager.invokeClient(actions = ClientAction.Cleanup)
    }

    suspend fun handlePendingMessages(messages: List<PendingMessageDto>) {
        val messageEntities = messages
            .mapNotNull { message -> onionParsingService.parse(message) }
            .mapNotNull { item -> parseArtifact(item) }
        saveIncomingMessages(messageEntities)
        workManager.invokeClient(actions = ClientAction.Cleanup)
    }

    suspend fun saveOutgoingMessage(
        message: MessageDto,
        additionalDestinations: Set<String> = hashSetOf(),
        attachment: AttachmentDto? = null
    ): Boolean {
        try {
            val entity = message.withSenderAddress(localAddress).run {
                if (isDeleteOrDeleteAll()) {
                    markAsDeleted()
                } else {
                    this
                }
            }
            val messageId = repository.local.insertOrIgnoreMessage(entity)
            if (messageId != null) {
                if (attachment != null) {
                    repository.local.insertOrUpdateAttachment(attachment.copy(messageId = messageId))
                }
                workManager.sendMessage(
                    messageId = messageId,
                    additionalDestinations = additionalDestinations
                )
                return true
            }
            return false
        } catch (_: Exception) {
            return false
        }
    }

    suspend fun saveOutgoingMessages(messages: List<MessageDto>): Boolean {
        return messages.map { message -> saveOutgoingMessage(message) }.all { it }
    }

    private suspend fun createOrUpdateContact(artifactDto: ArtifactDto, publicKey: String) {
        val originAddress = publicKey.getAddressFromPublicKey() ?: return
        val contact =
            repository.local.getContact(originAddress) ?: ContactDtoFactory.createContact(
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
                }
        repository.local.insertOrUpdateContact(updatedContact)
    }

    private suspend fun createAttachmentEntity(artifactDto: ArtifactDto, messageId: Long) {
        repository.local.insertOrUpdateAttachment(
            AttachmentDto(
                messageId = messageId,
                path = null,
                url = artifactDto.resourceUrl,
                passphrase = artifactDto.passphrase
            )
        )
    }

    private suspend fun downloadGroupData(artifactDto: ArtifactDto, messageId: Long) {
        createAttachmentEntity(artifactDto, messageId)
        workManager.downloadGroupData(messageId)
    }

    private suspend fun downloadAttachment(artifactDto: ArtifactDto, messageId: Long) {
        createAttachmentEntity(artifactDto, messageId)
        workManager.downloadAttachment(messageId)
    }

    private suspend fun saveIncomingMessages(messages: List<Triple<SignatureDto, ArtifactDto, MessageDto>>) {
        return messages.forEach { item -> saveIncomingMessage(item) }
    }

    private suspend fun notify(message: MessageDto) {
        val contact = repository.local.getContact(message.chatId) ?: return
        notifier.notifyNewMessage(contact, message)
    }
}
