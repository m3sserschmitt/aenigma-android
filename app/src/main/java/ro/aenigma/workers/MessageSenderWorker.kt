/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.services.PathFinder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import ro.aenigma.R
import ro.aenigma.models.AttachmentDto
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ContactWithGroupDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithAttachmentsDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.GuardDtoExtensions.getHostname
import ro.aenigma.models.extensions.MessageDtoExtensions.isDeleteOrDeleteAll
import ro.aenigma.models.extensions.MessageDtoExtensions.markAsDeleted
import ro.aenigma.models.extensions.MessageDtoExtensions.markAsSent
import ro.aenigma.models.extensions.MessageDtoExtensions.toArtifactDto
import ro.aenigma.services.Notifier
import ro.aenigma.services.SignalrController
import ro.aenigma.util.Constants.Companion.BROADCAST_CONTACT_ADDRESS
import ro.aenigma.util.Constants.Companion.ENCRYPTION_KEY_SIZE
import ro.aenigma.util.Constants.Companion.SEND_MESSAGES_CHUNK_SIZE
import ro.aenigma.util.ContextExtensions.createZip
import ro.aenigma.util.ContextExtensions.getCacheFile
import ro.aenigma.util.SerializerExtensions.toCanonicalJson
import ro.aenigma.workers.extensions.WorkManagerExtensions.sendMessage

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalrController: SignalrController,
    private val repository: Repository,
    private val signatureService: SignatureService,
    private val notifier: Notifier,
    private val pathFinder: PathFinder
) : CoroutineWorker(context, params) {

    companion object {
        const val MESSAGE_ID_ARG = "message-id"
        const val ADDITIONAL_DESTINATIONS_ARG = "additional-destinations"
        const val DESTINATIONS_ARG = "destinations"
        const val UNIQUE_WORK_REQUEST_NAME = "message-sender-worker"
        private const val MAX_RETRY_COUNT = 3

        fun getUniqueWorkRequestName(messageId: Long, destinations: List<String>): String {
            return if (destinations.isNotEmpty()) {
                "$UNIQUE_WORK_REQUEST_NAME-$messageId-${
                    destinations.toTypedArray().contentHashCode()
                }"
            } else {
                "$UNIQUE_WORK_REQUEST_NAME-$messageId"
            }
        }

        fun getTag(messageId: Long): String {
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
            message.markAsSent().run {
                if (isDeleteOrDeleteAll()) {
                    markAsDeleted()
                } else {
                    this
                }
            })
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
            || !messageWithAttachment.attachment?.url.isNullOrBlank()
        ) {
            return messageWithAttachment
        }

        setForeground(getForegroundInfo())

        val archive = if (!messageWithAttachment.attachment?.path.isNullOrBlank()) {
            val cachedArchive =
                applicationContext.getCacheFile(messageWithAttachment.attachment.path)
            if (!cachedArchive.exists()) {
                applicationContext.createZip(uris = messageWithAttachment.message.files)
            } else {
                cachedArchive
            }
        } else {
            applicationContext.createZip(uris = messageWithAttachment.message.files)
        } ?: return null

        var attachment = AttachmentDto(
            messageId = messageWithAttachment.message.id,
            path = archive.name,
            url = null,
            passphrase = null
        )
        repository.local.insertOrUpdateAttachment(attachment)

        val key = CryptoProvider.generateRandomBytes(ENCRYPTION_KEY_SIZE)
        val useTor = repository.local.useTor.firstOrNull() == true
        val useOrbot = repository.local.useOrbot.firstOrNull() == true
        val usingTor = useOrbot || useTor
        val createdSharedData =
            repository.remote.postEncryptedFile(archive, accessCount, key, usingTor) { progress ->
                notifier.notifyUploadProgress(progress, id.hashCode())
            }
        createdSharedData?.resourceUrl ?: return null
        archive.delete()

        attachment = attachment.copy(
            url = createdSharedData.resourceUrl,
            passphrase = CryptoProvider.base64Encode(key)
        )
        repository.local.insertOrUpdateAttachment(attachment)

        return messageWithAttachment.copy(
            attachment = attachment
        )
    }

    private suspend fun getDestinationContacts(
        contactWithGroup: ContactWithGroupDto?,
        destinations: Array<String>,
        additionalDestinations: Array<String>,
        isBroadcast: Boolean
    ): List<ContactDto> {
        val addressesSet = mutableSetOf<String>()
        val results = mutableListOf<ContactDto>()
        when {
            destinations.isNotEmpty() -> {
                destinations.forEach { address ->
                    if (addressesSet.add(address)) {
                        repository.local.getContact(address)?.let { c -> results.add(c) }
                    }
                }
            }

            isBroadcast -> {
                return repository.local.getAllContacts()
            }

            contactWithGroup?.contact?.type == ContactType.CONTACT -> {
                addressesSet.add(contactWithGroup.contact.address)
                results.add(contactWithGroup.contact)
            }

            contactWithGroup?.contact?.type == ContactType.GROUP -> {
                contactWithGroup.group?.groupData?.members?.forEach { item ->
                    if (!item.address.isNullOrBlank()
                        && item.address != signatureService.address
                        && addressesSet.add(item.address)
                    ) {
                        repository.local.getContact(item.address)?.let { c -> results.add(c) }
                    }
                }
            }
        }

        additionalDestinations.forEach { address ->
            if (!addressesSet.contains(address)) {
                repository.local.getContact(address)?.let { c -> results.add(c) }
            }
        }
        return results
    }

    private fun scheduleChildWorkers(
        messageId: Long,
        contacts: List<ContactDto>,
        destinations: Array<String>
    ): Boolean {
        return if (destinations.isEmpty()) {
            val workManager = WorkManager.getInstance(applicationContext)
            contacts.chunked(SEND_MESSAGES_CHUNK_SIZE).forEach { chunk ->
                workManager.sendMessage(
                    messageId = messageId,
                    destinations = chunk.mapTo(mutableSetOf()) { item -> item.address }
                )
            }
            true
        } else {
            false
        }
    }

    private fun getFailureOutputData(messageId: Long, destinations: Array<String>): Result {
        return Result.failure(
            workDataOf(
                MESSAGE_ID_ARG to messageId,
                DESTINATIONS_ARG to destinations
            )
        )
    }

    override suspend fun doWork(): Result {
        val messageId = inputData.getLong(MESSAGE_ID_ARG, Long.MIN_VALUE)
        val destinations = inputData.getStringArray(DESTINATIONS_ARG) ?: arrayOf()

        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return getFailureOutputData(messageId, destinations)
        }

        if (!pathFinder.load()) {
            return Result.retry()
        }

        val additionalDestinations =
            inputData.getStringArray(ADDITIONAL_DESTINATIONS_ARG) ?: arrayOf()

        val userName = repository.local.name.first()
        val messageToBeSent = if (messageId > 0) {
            repository.local.getMessageWithAttachments(messageId)
        } else {
            null
        }
        val chatId =
            messageToBeSent?.message?.chatId ?: return getFailureOutputData(messageId, destinations)
        if (messageToBeSent.message.sent) {
            return Result.success()
        }

        val isBroadcast = chatId == BROADCAST_CONTACT_ADDRESS
        val contactWithGroup = repository.local.getContactWithGroup(chatId)
            ?: if (!isBroadcast) {
                return getFailureOutputData(messageId, destinations)
            } else {
                null
            }
        val contacts = getDestinationContacts(
            contactWithGroup,
            destinations,
            additionalDestinations,
            isBroadcast
        )

        val messageWithAttachments = if (!contacts.isEmpty()) {
            resolveAttachments(messageToBeSent, contacts.size)
        } else {
            messageToBeSent
        } ?: return Result.retry()

        if (scheduleChildWorkers(messageId, contacts, destinations)) {
            return Result.success()
        }

        val ok = contacts.isEmpty() || sendMessage(
            contacts = contacts,
            message = messageWithAttachments.message,
            userName = userName,
            groupAddress = contactWithGroup?.group?.address,
            resourceUrl = messageWithAttachments.attachment?.url
                ?: contactWithGroup?.group?.resourceUrl,
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
            id.hashCode(),
            notifier.createWorkerNotification(applicationContext.getString(R.string.sending_message)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.sending_message))
            )
    }
}
