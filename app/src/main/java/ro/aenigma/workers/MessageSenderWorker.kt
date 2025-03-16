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
import ro.aenigma.crypto.AddressExtensions.isValidAddress
import ro.aenigma.crypto.Base64Extensions.isValidBase64
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.PublicKeyExtensions.publicKeyMatchAddress
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.VertexEntity
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.models.MessageWithMetadata
import ro.aenigma.models.Vertex
import ro.aenigma.services.PathFinder
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.models.MessageActionDto
import ro.aenigma.models.enums.ContactType
import java.time.ZonedDateTime
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
//        private const val MIN_CONTACT_SYNC_INTERVAL_MINUTES: Long = 15
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
        if (path.isEmpty()) {
            return null
        }

        val guard = repository.local.getGuard() ?: return null
        val localAddress = groupAddress ?: (signatureService.address ?: return null)
        val reversedPath = path.reversed()
        val addresses =
            arrayOf(localAddress, destination.address) + reversedPath.map { item -> item.address }
                .subList(0, reversedPath.size - 1)
        val keys = arrayOf(destination.publicKey) + reversedPath.map { item -> item.publicKey }
        val action = MessageActionDto(
            actionType = message.action.actionType,
            refId = message.action.refId
        )
        val messageDetails = MessageWithMetadata(
            text = message.text,
            action = action,
            senderName = userName,
            senderGuardAddress = guard.address,
            senderGuardHostname = guard.hostname,
            senderPublicKey = signatureService.publicKey, // TODO: public key & guard should not be sent in every message
            refId = message.refId,
            groupResourceUrl = groupResourceUrl,
        )
        val serializedData = Gson().toJson(messageDetails).toByteArray()
        return CryptoProvider.sealOnionEx(serializedData, keys, addresses)
    }

    private fun validateVertex(
        vertex: Vertex?,
        isLeaf: Boolean,
        publicKey: String? = null
    ): Boolean {
        if (vertex == null) {
            return false
        }
        val key = if(publicKey.isNullOrBlank()) vertex.publicKey else publicKey
        if (!key.isValidPublicKey()) {
            return false
        }
        if ((isLeaf && vertex.neighborhood?.neighbors?.count() != 1)
            || vertex.neighborhood?.neighbors?.all { item -> item.isValidAddress() } != true
        ) {
            return false
        }
        if (!vertex.neighborhood.address.isValidAddress()) {
            return false
        }
        if (!isLeaf && !key.publicKeyMatchAddress(vertex.neighborhood.address)) {
            return false
        }
        return vertex.signedData.isValidBase64() && CryptoProvider.verifyEx(
            key!!,
            vertex.signedData!!
        )
    }

    private suspend fun updateContactIfRequired(contactEntity: ContactEntity): Boolean {
//        val threshold = ZonedDateTime.now().minusMinutes(MIN_CONTACT_SYNC_INTERVAL_MINUTES)
//        val aboveThreshold = contactEntity.lastSynchronized.isBefore(threshold)
//        if (!aboveThreshold) {
//            return true
//        }

        if(contactEntity.guardAddress.isValidAddress())
        {
            return true
        }
        try {
            val vertexResponse = repository.remote.getVertex(contactEntity.address)
            val vertex = vertexResponse.body()

            if (vertexResponse.code() != 200 || !validateVertex(
                    vertex,
                    true,
                    contactEntity.publicKey
                )
            ) {
                return false
            }

            val guardAddress = vertex?.neighborhood?.neighbors?.firstOrNull() ?: return false
            val guardResponse = repository.remote.getVertex(guardAddress)
            val guard = guardResponse.body()

            if (guardResponse.code() != 200 || !validateVertex(guard, false)) {
                return false
            }

            contactEntity.guardAddress = guard!!.neighborhood!!.address!!
            contactEntity.guardHostname = guard.neighborhood!!.hostname
            contactEntity.lastSynchronized = ZonedDateTime.now()

            repository.local.updateContact(contactEntity)
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

        message.sent = true
        repository.local.updateMessage(message)

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
