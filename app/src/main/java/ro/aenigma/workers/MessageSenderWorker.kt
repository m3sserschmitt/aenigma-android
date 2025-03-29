package ro.aenigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
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
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.models.MessageWithMetadata
import ro.aenigma.services.PathFinder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.extensions.SignatureExtensions.sign
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardAddress
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withGuardHostname
import ro.aenigma.data.database.extensions.MessageEntityExtensions.markAsSent
import ro.aenigma.models.enums.ContactType
import ro.aenigma.util.SerializerExtensions.toJson
import java.util.concurrent.TimeUnit

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalRClient: SignalRClient,
    private val repository: Repository,
    private val signatureService: SignatureService,
    private val pathFinder: PathFinder
) : CoroutineWorker(context, params) {

    companion object {
        const val USER_NAME_ARG = "UserName"
        const val MESSAGE_ID_ARG = "MessageId"
        const val RESOURCE_URL_ARG = "ResourceUrl"
        private const val UNIQUE_WORK_REQUEST_NAME = "MessageSenderWorkRequest"
        private const val DELAY_BETWEEN_RETRIES: Long = 10
        private const val MAX_RETRY_COUNT = 5

        @JvmStatic
        fun createWorkRequest(workManager: WorkManager, messageId: Long, userName: String, resourceUrl: String? = null) {
            val parameters = Data.Builder()
                .putString(USER_NAME_ARG, userName)
                .putLong(MESSAGE_ID_ARG, messageId)
                .putString(RESOURCE_URL_ARG, resourceUrl)
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
        groupResourceUrl: String?
    ): String? {
        if (path.isEmpty() || destination.publicKey == null) {
            return null
        }

        val guard = repository.local.getGuard() ?: return null
        val localAddress = groupAddress ?: (signatureService.address ?: return null)
        val reversedPath = path.reversed()
        val addresses =
            arrayOf(localAddress, destination.address) + reversedPath.map { item -> item.address }
                .subList(0, reversedPath.size - 1)
        val keys = arrayOf(destination.publicKey) + reversedPath.map { item -> item.publicKey }
        val message = MessageWithMetadata(
            text = message.text,
            type = message.type,
            actionFor = message.actionFor,
            senderName = userName,
            senderGuardAddress = guard.address,
            senderGuardHostname = guard.hostname,
            senderPublicKey = signatureService.publicKey,
            refId = message.refId,
            groupResourceUrl = groupResourceUrl
        ).sign(signatureService).toJson()?.toByteArray() ?: return null
        return CryptoProvider.sealOnionEx(message, keys, addresses)
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

    private suspend fun sendMessage(
        contacts: List<ContactEntity>,
        message: MessageEntity,
        userName: String?,
        groupAddress: String?,
        groupResourceUrl: String?
    ): Result {
        val onions = contacts.mapNotNull { contact ->
            if(updateContactIfRequired(contact)) {
                val paths = pathFinder.calculatePaths(contact)
                if (paths.isNotEmpty()) {
                    buildOnion(message, contact, userName, paths.random().vertexList, groupAddress,
                        groupResourceUrl
                    )
                } else null
            } else null
        }
        if (!signalRClient.sendMessages(onions)) {
            return Result.retry()
        }

        val entity = message.markAsSent() ?: return Result.success()
        repository.local.updateMessage(entity)
        return Result.success()
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount > MAX_RETRY_COUNT) {
            return Result.failure()
        }

        if (!signalRClient.isConnected()) {
            return Result.retry()
        }

        if (!pathFinder.load()) {
            return Result.retry()
        }

        val messageId = inputData.getLong(MESSAGE_ID_ARG, -1)
        val userName = inputData.getString(USER_NAME_ARG) ?: return Result.failure()
        val resourceUrl = inputData.getString(RESOURCE_URL_ARG)
        val message = if (messageId > 0) repository.local.getMessage(messageId) else null
        val chatId = message?.chatId ?: return Result.failure()
        val contact = repository.local.getContactWithGroup(chatId) ?: return Result.failure()
        val contacts = (if (contact.contact.type == ContactType.CONTACT) listOf(contact.contact)
        else contact.group?.groupData?.members?.mapNotNull { item ->
            val address = item.publicKey.getAddressFromPublicKey()
            if (address != null && address != signatureService.address) {
                repository.local.getContact(
                    address
                )
            } else {
                null
            }
        }) ?: return Result.failure()
        if(contacts.isEmpty())
        {
            return Result.success()
        }
        return sendMessage(
            contacts = contacts,
            message = message,
            userName = userName,
            groupAddress = contact.group?.address,
            groupResourceUrl = resourceUrl ?: contact.group?.resourceUrl
        )
    }
}
