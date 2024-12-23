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
import ro.aenigma.crypto.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.VertexEntity
import ro.aenigma.data.network.SignalRClient
import ro.aenigma.models.OnionDetails
import ro.aenigma.models.Vertex
import ro.aenigma.routing.PathFinder
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val signalRClient: SignalRClient,
    private val repository: Repository,
    private val signatureService: SignatureService,
    private val pathFinder: PathFinder
) : CoroutineWorker(context, params) {

    companion object {
        const val MESSAGE_ID = "messageId"

        private const val MIN_CONTACT_SYNC_INTERVAL_MINUTES: Long = 15
        private const val DELAY_BETWEEN_RETRIES: Long = 10
        private const val MAX_RETRY_COUNT = 5

        @JvmStatic
        fun sendMessage(context: Context, message: MessageEntity) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val parameters = Data.Builder()
                .putLong(MESSAGE_ID, message.id)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<MessageSenderWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "MessageSenderWorkRequest-${message.id}",
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
        }
    }

    private suspend fun buildOnion(
        text: String,
        destination: ContactEntity,
        path: List<VertexEntity>
    ): String? {
        if (text.isBlank() || path.isEmpty()) {
            return null
        }

        val guard = repository.local.getGuard() ?: return null
        val localAddress = signatureService.address ?: return null
        val reversedPath = path.reversed()
        val addresses =
            arrayOf(localAddress, destination.address) + reversedPath.map { item -> item.address }
                .subList(0, reversedPath.size - 1)
        val keys = arrayOf(destination.publicKey) + reversedPath.map { item -> item.publicKey }

        val json = Gson()
        val messageDetails = OnionDetails(
            text,
            signatureService.address,
            guard.address,
            guard.hostname,
            signatureService.publicKey ?: "" // TODO: public key shout not be sent in every message
        )
        val serializedData = json.toJson(messageDetails)

        return CryptoProvider.sealOnionEx(serializedData.toByteArray(), keys, addresses)
    }

    private fun validateVertex(
        vertex: Vertex?,
        isLeaf: Boolean,
        publicKey: String? = null
    ): Boolean {
        if (vertex == null) {
            return false
        }
        val key = publicKey ?: vertex.publicKey
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
        val threshold = ZonedDateTime.now().minusMinutes(MIN_CONTACT_SYNC_INTERVAL_MINUTES)
        val aboveThreshold = contactEntity.lastSynchronized.isBefore(threshold)
        if (!aboveThreshold) {
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

    override suspend fun doWork(): Result {
        if (runAttemptCount > MAX_RETRY_COUNT) {
            return Result.failure()
        }

        val messageId = inputData.getLong(MESSAGE_ID, -1)

        if (messageId < 0) {
            return Result.failure()
        }

        if (!signalRClient.isConnected()) {
            return Result.retry()
        }

        if (!pathFinder.load()) {
            return Result.retry()
        }

        val message = repository.local.getMessage(messageId) ?: return Result.failure()
        val contact = repository.local.getContact(message.chatId) ?: return Result.failure()

        if (!updateContactIfRequired(contact)) {
            return Result.failure()
        }

        val paths = pathFinder.calculatePaths(contact)

        if (paths.isEmpty()) {
            return Result.failure()
        }

        val path = paths.random().vertexList
        val onion = buildOnion(message.text, contact, path) ?: return Result.failure()

        if (!signalRClient.sendMessage(onion)) {
            return Result.retry()
        }

        message.sent = true
        repository.local.updateMessage(message)

        return Result.success()
    }
}
