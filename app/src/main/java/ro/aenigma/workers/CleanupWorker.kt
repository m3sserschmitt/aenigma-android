package ro.aenigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.data.Repository

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            repository.local.removeMessagesHard()
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
