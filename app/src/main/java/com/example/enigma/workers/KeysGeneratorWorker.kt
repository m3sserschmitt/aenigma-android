package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.enigma.crypto.KeyGenerator
import com.example.enigma.data.Repository
import com.example.enigma.data.database.KeyPairEntity
import com.example.enigma.util.AddressHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltWorker
class KeysGeneratorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository
) : Worker(context, params){

    override fun doWork(): Result {

        val keypair = KeyGenerator.keyPairToPEM()

        CoroutineScope(Dispatchers.IO).launch {
            repository.local.insertKeyPair(KeyPairEntity(
                keypair.first,
                keypair.second,
                AddressHelper.getHexAddressFromPublicKey(keypair.first)
            ))
        }

        return Result.success()
    }
}
