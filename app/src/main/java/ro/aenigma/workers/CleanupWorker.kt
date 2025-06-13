package ro.aenigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.data.Repository
import java.util.concurrent.TimeUnit

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository
) : CoroutineWorker(context, params) {
    companion object {
        private const val REPEAT_INTERVAL_DAYS: Long = 3
        private const val UNIQUE_PERIODIC_WORK_REQUEST = "CleanupWorkRequest"

        @JvmStatic
        fun scheduleCleanup(context: Context) {
            val signalRClientRequest = PeriodicWorkRequest.Builder(
                CleanupWorker::class.java,
                REPEAT_INTERVAL_DAYS,
                TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK_REQUEST,
                    ExistingPeriodicWorkPolicy.KEEP,
                    signalRClientRequest
                )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            repository.local.removeMessagesHard()
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
