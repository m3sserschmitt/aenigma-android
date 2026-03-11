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
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.services.PathFinder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import ro.aenigma.R
import ro.aenigma.models.AttachmentDto
import ro.aenigma.models.AttachmentsMetadataDto
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ContactWithGroupDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithAttachmentsDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.GuardDtoExtensions.getHostname
import ro.aenigma.models.extensions.MessageDtoExtensions.isDelete
import ro.aenigma.models.extensions.MessageDtoExtensions.markAsDeleted
import ro.aenigma.models.extensions.MessageDtoExtensions.markAsSent
import ro.aenigma.models.extensions.MessageDtoExtensions.toArtifactDto
import ro.aenigma.services.NotificationService
import ro.aenigma.services.SignalrController
import ro.aenigma.services.Zipper
import ro.aenigma.util.Constants.Companion.ENCRYPTION_KEY_SIZE
import ro.aenigma.util.Constants.Companion.MESSAGE_SENDER_NOTIFICATION_ID
import ro.aenigma.util.SerializerExtensions.toCanonicalJson
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val zipper: Zipper,
    private val signalrController: SignalrController,
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
        message: MessageDto,
        destination: ContactDto,
        userName: String?,
        path: List<VertexDto>,
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
            arrayOf(chatId) + reversedPath.take(path.size - 2).map { item -> item.address!! }
        val keys =
            reversedPath.take(path.size - 1).map { vertex -> vertex.publicKey!! }.toTypedArray()
        val data = signatureService.jsonSign(
            message.toArtifactDto(
                senderName = userName,
                guardAddress = guard.address,
                guardHostname = guard.getHostname(),
                resourceUrl = groupResourceUrl,
                chatId = chatId,
                passphrase = passphrase
            )
        ).toCanonicalJson()?.toByteArray() ?: return null
        return CryptoProvider.sealOnionEx(data, keys, addresses)
    }

    private suspend fun saveAsSent(message: MessageDto) {
        repository.local.updateMessage(
            message.markAsSent().run { if (isDelete()) markAsDeleted() else this })
    }

    private suspend fun sendMessage(
        contacts: List<ContactDto>,
        message: MessageDto,
        userName: String?,
        groupAddress: String?,
        resourceUrl: String?,
        passphrase: String?
    ): Boolean {
        val onions = contacts.mapNotNull { contact ->
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
        }
        return signalrController.sendMessages(onions)
    }

    suspend fun resolveAttachments(
        messageWithAttachment: MessageWithAttachmentsDto,
        accessCount: Int
    ): MessageWithAttachmentsDto? {
        if (messageWithAttachment.message.type != MessageType.FILES
            || messageWithAttachment.message.files.isNullOrEmpty()
            || messageWithAttachment.attachment?.url != null
        ) {
            return messageWithAttachment
        }

        setForeground(getForegroundInfo())

        val archive = if (messageWithAttachment.attachment?.path != null)
            File(applicationContext.cacheDir, messageWithAttachment.attachment.path)
        else
            zipper.createZip(
                messageWithAttachment.message.files, AttachmentsMetadataDto(
                    description = messageWithAttachment.message.text,
                    filesCount = messageWithAttachment.message.files.size
                )
            ) ?: return null

        val attachment = AttachmentDto(
            messageId = messageWithAttachment.message.id,
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
        encryptedFile.delete()

        val finalAttachment = attachment.copy(
            url = createdSharedData.resourceUrl,
            passphrase = CryptoProvider.base64Encode(passphrase)
        )
        repository.local.insertOrUpdateAttachment(finalAttachment)

        return MessageWithAttachmentsDto(
            message = messageWithAttachment.message,
            attachment = finalAttachment
        )
    }

    private suspend fun getDestinationContacts(
        contactWithGroup: ContactWithGroupDto,
        additionalDestinations: Array<String>?
    ): List<ContactDto> {
        val addressesSet = hashSetOf<String>()
        val results = mutableListOf<ContactDto>()
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
        if (messageToBeSent.message.sent) {
            return Result.success()
        }
        val contactWithGroup =
            repository.local.getContactWithGroup(chatId) ?: return Result.failure()
        val contacts = getDestinationContacts(contactWithGroup, additionalDestinations)
        val messageWithAttachments = (
                if (!contacts.isEmpty()) {
                    resolveAttachments(messageToBeSent, contacts.size)
                } else {
                    messageToBeSent
                })
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
