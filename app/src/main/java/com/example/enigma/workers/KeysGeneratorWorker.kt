package com.example.enigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.enigma.crypto.KeysManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class KeysGeneratorWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : Worker(context, params){

    override fun doWork(): Result {
        return if(KeysManager.generateKeys(context)) Result.success() else Result.failure()
    }
}
