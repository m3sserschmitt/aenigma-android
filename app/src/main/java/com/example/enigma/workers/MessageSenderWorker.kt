package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.models.MessageBase
import com.example.enigma.models.MessageExtended
import com.example.enigma.util.AddressHelper
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.util.Date

@HiltWorker
class MessageSenderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val signalRClient: SignalRClient,
    private val repository: Repository,
    private val addressProvider: AddressProvider
) : CoroutineWorker(context, params) {

    companion object {
        const val DATA_PARAM = "data"
        const val DESTINATION_PARAM = "destination"
        const val INCLUDE_PUBLIC_KEY = "includePublicKey"

        private const val MAX_RETRIES: Int = 5
        private const val DELAY_TIME: Long = 5000
    }

    override suspend fun doWork(): Result {

        var count = 0
        while(!signalRClient.isConnected() && count < MAX_RETRIES)
        {
            if(count < 1) signalRClient.createConnection()
            count ++
            delay(DELAY_TIME)
        }

        if(!signalRClient.isConnected()) return Result.failure()

        val destinationAddress = inputData.getString(DESTINATION_PARAM) ?: return Result.failure()
        val paths = repository.local.getGraphPath(destinationAddress)

        if(paths.isEmpty())
        {
            return Result.failure()
        }

        val data = inputData.getString(DATA_PARAM) ?: return Result.failure()
        val localAddress = addressProvider.address ?: return Result.failure()
        val includePublicKey = inputData.getBoolean(INCLUDE_PUBLIC_KEY, false)
        val path = paths[0].path
        val localPublicKey = if (addressProvider.publicKey != null) addressProvider.publicKey!! else ""
        val addresses = arrayOf(localAddress) + path.subList(0, path.size - 1)
                    .map { item -> AddressHelper.getHexAddressFromPublicKey(item) }

        val json = Gson()
        val message = if (includePublicKey)
            json.toJson(MessageExtended(
                data,
                localPublicKey,
                AddressHelper.getHexAddressFromPublicKey(path.last()))
            ) else
            json.toJson(MessageBase(data))

        val onion = CryptoProvider.buildOnion(message.toByteArray(), path.toTypedArray(), addresses)
            ?: return Result.failure()

        if(!signalRClient.sendMessage(onion)) return Result.failure()

        repository.local.insertMessage(
            MessageEntity(
                destinationAddress,
                data,
                false,
                Date()
            )
        )

        return Result.success()
    }
}
