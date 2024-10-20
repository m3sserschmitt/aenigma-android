package com.example.enigma.workers

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
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.database.VertexEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.models.OnionDetails
import com.example.enigma.routing.PathFinder
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val signalRClient: SignalRClient,
    private val repository: Repository,
    private val addressProvider: AddressProvider,
    private val pathFinder: PathFinder
) : CoroutineWorker(context, params) {

    companion object {
        const val MESSAGE_ID = "messageId"

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

    private suspend fun buildOnion(text: String, destination: ContactEntity, path: List<VertexEntity>): String?
    {
        if(text.isBlank() || path.isEmpty())
        {
            return null
        }

        val guard = repository.local.getGuard() ?: return null
        val localAddress = addressProvider.address ?: return null
        val reversedPath = path.reversed()
        val addresses = arrayOf(localAddress, destination.address) + reversedPath.map { item -> item.address }.subList(0, reversedPath.size - 1)
        val keys = arrayOf(destination.publicKey) + reversedPath.map { item -> item.publicKey }

        val json = Gson()
        val messageDetails = OnionDetails(
            text,
            addressProvider.address ?: "",
            guard.address,
            guard.hostname,
            addressProvider.publicKey ?: "" // TODO: public key shout not be sent in every message
        )
        val serializedData = json.toJson(messageDetails)

        return CryptoProvider.buildOnion(
            serializedData.toByteArray(),
            keys,
            addresses)
    }

    override suspend fun doWork(): Result {
        if(runAttemptCount > MAX_RETRY_COUNT)
        {
            return Result.failure()
        }

        val messageId = inputData.getLong(MESSAGE_ID, -1)

        if(messageId < 0)
        {
            return Result.failure()
        }

        if(!signalRClient.isConnected())
        {
            return Result.retry()
        }

        if(!pathFinder.load())
        {
            return Result.retry()
        }

        val message = repository.local.getMessage(messageId) ?: return Result.failure()
        val contact = repository.local.getContact(message.chatId) ?: return Result.failure()
        val paths = pathFinder.calculatePaths(contact)

        if(paths.isEmpty())
        {
            return Result.failure()
        }

        val path = paths.random().vertexList
        val onion = buildOnion(message.text, contact, path) ?: return Result.failure()

        if(!signalRClient.sendMessage(onion))
        {
            return Result.retry()
        }

        message.sent = true
        repository.local.updateMessage(message)

        return Result.success()
    }
}
