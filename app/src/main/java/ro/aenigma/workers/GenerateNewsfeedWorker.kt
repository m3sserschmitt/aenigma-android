package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.R
import ro.aenigma.data.Repository
import ro.aenigma.services.FeedSampler
import ro.aenigma.services.Notifier

@HiltWorker
class GenerateNewsfeedWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val feedSampler: FeedSampler,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_REQUEST_NAME = "generate-feed-worker"

        const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }
        setForeground(getForegroundInfo())
        repository.local.saveNewsFeed(feedSampler.getFeed())
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ForegroundInfo(
            id.hashCode(),
            notifier.createWorkerNotification(applicationContext.getString(R.string.generate_feed)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.generate_feed))
            )
    }
}
