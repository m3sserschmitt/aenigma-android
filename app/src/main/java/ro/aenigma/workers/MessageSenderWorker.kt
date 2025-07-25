package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ro.aenigma.crypto.extensions.AddressExtensions.isValidAddress
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.VertexEntity
import ro.aenigma.services.PathFinder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import ro.aenigma.R
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.MessageWithAttachments
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardAddress
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardHostname
import ro.aenigma.data.database.extensions.MessageEntityExtensions.isDelete
import ro.aenigma.data.database.extensions.MessageEntityExtensions.markAsDeleted
import ro.aenigma.data.database.extensions.MessageEntityExtensions.markAsSent
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toArtifact
import ro.aenigma.data.database.factories.AttachmentEntityFactory
import ro.aenigma.models.AttachmentsMetadata
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.services.NotificationService
import ro.aenigma.services.SignalrConnectionController
import ro.aenigma.services.Zipper
import ro.aenigma.util.Constants.Companion.ENCRYPTION_KEY_SIZE
import ro.aenigma.util.Constants.Companion.MESSAGE_SENDER_NOTIFICATION_ID
import ro.aenigma.util.SerializerExtensions.toJson
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val zipper: Zipper,
    private val signalrController: SignalrConnectionController,
    private val repository: Repository,
    private val signatureService: SignatureService,
    private val notificationService: NotificationService,
    private val pathFinder: PathFinder
) : CoroutineWorker(context, params) {

    companion object {
        private const val MESSAGE_ID_ARG = "MessageId"
        private const val ADDITIONAL_DESTINATIONS_ARG = "AdditionalDestinations"
        private const val UNIQUE_WORK_REQUEST_NAME = "MessageSenderWorkRequest"
        private const val DELAY_BETWEEN_RETRIES: Long = 5
        private const val MAX_RETRY_COUNT = 5

        @JvmStatic
        fun createWorkRequest(
            workManager: WorkManager,
            messageId: Long,
            additionalDestinations: Set<String> = hashSetOf()
        ): UUID {
            val parameters = Data.Builder()
                .putLong(MESSAGE_ID_ARG, messageId)
                .putStringArray(ADDITIONAL_DESTINATIONS_ARG, additionalDestinations.toTypedArray())
                .build()
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<MessageSenderWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(
                getUniqueWorkRequestName(messageId),
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            return workRequest.id
        }

        fun getUniqueWorkRequestName(messageId: Long): String {
            return "$UNIQUE_WORK_REQUEST_NAME-$messageId"
        }
    }

    private suspend fun buildOnion(
        message: MessageEntity,
        destination: ContactEntity,
        userName: String?,
        path: List<VertexEntity>,
        groupAddress: String?,
        groupResourceUrl: String?,
        passphrase: String?
    ): String? {
        if (path.isEmpty() || destination.publicKey == null) {
            return null
        }

        val guard = repository.local.getGuard() ?: return null
        val chatId = groupAddress ?: (signatureService.address ?: return null)
        val reversedPath = path.reversed()
        val addresses =
            arrayOf(chatId) + reversedPath.take(path.size - 2).map { item -> item.address }
        val keys =
            reversedPath.take(path.size - 1).map { vertex -> vertex.publicKey }.toTypedArray()
        val data = signatureService.jsonSign(
            message.toArtifact(
                senderName = userName,
                guardAddress = guard.address,
                guardHostname = guard.hostname,
                resourceUrl = groupResourceUrl,
                chatId = chatId,
                passphrase = passphrase
            )
        ).toJson()?.toByteArray() ?: return null
        return CryptoProvider.sealOnionEx(data, keys, addresses)
    }

    private suspend fun updateContactIfRequired(contactEntity: ContactEntity): Boolean {
        if (contactEntity.guardAddress.isValidAddress()) {
            return true
        }
        try {
            val vertex =
                repository.remote.getVertex(contactEntity.address, true, contactEntity.publicKey)
                    ?: return false
            val guardAddress = vertex.neighborhood?.neighbors?.singleOrNull() ?: return false
            val guardVertex = repository.remote.getVertex(guardAddress, false) ?: return false
            val updatedContact = contactEntity.withGuardAddress(guardVertex.neighborhood?.address)
                .withGuardHostname(guardVertex.neighborhood?.hostname)
            updatedContact?.let { repository.local.updateContact(it) }
        } catch (_: Exception) {
            return false
        }
        return true
    }

    private suspend fun saveAsSent(message: MessageEntity) {
        repository.local.updateMessage(
            message.markAsSent()?.run { if (isDelete()) markAsDeleted() else this } ?: return)
    }

    private suspend fun sendMessage(
        contacts: List<ContactEntity>,
        message: MessageEntity,
        userName: String?,
        groupAddress: String?,
        resourceUrl: String?,
        passphrase: String?
    ): Boolean {
        val onions = contacts.mapNotNull { contact ->
            if (updateContactIfRequired(contact)) {
                val paths = pathFinder.calculatePaths(contact).filter { item ->
                    item.startVertex.address == signatureService.address
                            && item.endVertex.address == contact.address
                }
                if (paths.isNotEmpty()) {
                    buildOnion(
                        message = message,
                        destination = contact,
                        userName = userName,
                        path = paths.random().vertexList,
                        groupAddress = groupAddress,
                        groupResourceUrl = resourceUrl,
                        passphrase = passphrase
                    )
                } else null
            } else null
        }
        return signalrController.sendMessages(onions)
    }

    suspend fun resolveAttachments(
        messageWithAttachment: MessageWithAttachments,
        accessCount: Int
    ): MessageWithAttachments? {
        if (messageWithAttachment.message.type != MessageType.FILES
            || messageWithAttachment.message.files == null
            || messageWithAttachment.message.files.isEmpty()
            || messageWithAttachment.attachment?.url != null
        ) {
            return messageWithAttachment
        }

        setForeground(getForegroundInfo())

        val archive = if (messageWithAttachment.attachment?.path != null)
            File(applicationContext.cacheDir, messageWithAttachment.attachment.path)
        else
            zipper.createZip(
                messageWithAttachment.message.files, AttachmentsMetadata(
                    description = messageWithAttachment.message.text,
                    filesCount = messageWithAttachment.message.files.size
                )
            ) ?: return null

        val attachment = AttachmentEntityFactory.create(
            id = messageWithAttachment.message.id,
            path = archive.name,
            url = null,
            passphrase = null
        )
        repository.local.insertOrUpdateAttachment(attachment)

        val passphrase = CryptoProvider.generateRandomBytes(ENCRYPTION_KEY_SIZE)
        val encryptedFile = CryptoProvider.encrypt(archive, passphrase) ?: return null
        val createdSharedData = repository.remote.postFile(encryptedFile, accessCount)
        createdSharedData?.resourceUrl ?: return null

        archive.delete()

        val finalAttachment = attachment.copy(
            url = createdSharedData.resourceUrl,
            passphrase = CryptoProvider.base64Encode(passphrase)
        )
        repository.local.insertOrUpdateAttachment(finalAttachment)

        return MessageWithAttachments(
            message = messageWithAttachment.message,
            attachment = finalAttachment
        )
    }

    private suspend fun getDestinationContacts(
        contactWithGroup: ContactWithGroup,
        additionalDestinations: Array<String>?
    ): List<ContactEntity> {
        val addressesSet = hashSetOf<String>()
        val results = mutableListOf<ContactEntity>()
        if (contactWithGroup.contact.type == ContactType.CONTACT) {
            addressesSet.add(contactWithGroup.contact.address)
            results.add(contactWithGroup.contact)
        } else {
            contactWithGroup.group?.groupData?.members?.forEach { item ->
                if (item.address != null && item.address != signatureService.address && addressesSet.add(
                        item.address
                    )
                ) {
                    repository.local.getContact(item.address)?.let { c -> results.add(c) }
                }
            }
        }
        additionalDestinations?.forEach { address ->
            if (!addressesSet.contains(address)) {
                repository.local.getContact(address)?.let { c -> results.add(c) }
            }
        }
        return results
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }

        if (!signalrController.isConnected()) {
            return Result.retry()
        }

        if (!pathFinder.load()) {
            return Result.retry()
        }

        val messageId = inputData.getLong(MESSAGE_ID_ARG, Long.MIN_VALUE)
        val additionalDestinations = inputData.getStringArray(ADDITIONAL_DESTINATIONS_ARG)
        val userName = repository.local.name.first()
        val messageToBeSent =
            if (messageId > 0) repository.local.getMessageWithAttachments(messageId) else null
        val chatId = messageToBeSent?.message?.chatId ?: return Result.failure()
        val contactWithGroup =
            repository.local.getContactWithGroup(chatId) ?: return Result.failure()
        val contacts = getDestinationContacts(contactWithGroup, additionalDestinations)
        val messageWithAttachments =
            (if (!contacts.isEmpty()) resolveAttachments(
                messageToBeSent,
                contacts.size
            ) else messageToBeSent)
                ?: return Result.retry()

        val ok = contacts.isEmpty() || sendMessage(
            contacts = contacts,
            message = messageWithAttachments.message,
            userName = userName,
            groupAddress = contactWithGroup.group?.address,
            resourceUrl = messageWithAttachments.attachment?.url
                ?: contactWithGroup.group?.resourceUrl,
            passphrase = messageWithAttachments.attachment?.passphrase
        )
        return if (ok) {
            saveAsSent(messageWithAttachments.message)
            Result.success()
        } else {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ForegroundInfo(
            MESSAGE_SENDER_NOTIFICATION_ID,
            notificationService.createWorkerNotification(applicationContext.getString(R.string.sending_message)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                MESSAGE_SENDER_NOTIFICATION_ID,
                notificationService.createWorkerNotification(applicationContext.getString(R.string.sending_message))
            )
    }
}
