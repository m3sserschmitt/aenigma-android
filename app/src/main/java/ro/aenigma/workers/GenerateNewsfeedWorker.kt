package ro.aenigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.data.Repository
import ro.aenigma.services.FeedSampler

@HiltWorker
class GenerateNewsfeedWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val feedSampler: FeedSampler
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_REQUEST_NAME = "generate-feed-worker"

        const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }

        repository.local.saveNewsFeed(feedSampler.getFeed())
        return Result.success()
    }
}
