package ro.aenigma.workers.extensions

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ro.aenigma.models.enums.MessageType
import ro.aenigma.workers.AttachmentDownloadWorker
import ro.aenigma.workers.GenerateNewsfeedWorker
import ro.aenigma.workers.GraphReaderWorker
import ro.aenigma.workers.GroupDownloadWorker
import ro.aenigma.workers.GroupUploadWorker
import ro.aenigma.workers.MessageSenderWorker
import ro.aenigma.workers.SignalRClientWorker
import ro.aenigma.workers.SignalRWorkerAction
import java.util.UUID
import java.util.concurrent.TimeUnit

object WorkManagerExtensions {
    private const val DEFAULT_DELAY_BETWEEN_RETRY: Long = 2
    private val DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT = TimeUnit.SECONDS

    @JvmStatic
    private fun getGenerateFeedRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<GenerateNewsfeedWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                DEFAULT_DELAY_BETWEEN_RETRY,
                DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT
            ).build()
    }

    @JvmStatic
    fun getSyncGraphRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<GraphReaderWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                DEFAULT_DELAY_BETWEEN_RETRY,
                DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT
            ).build()
    }

    @JvmStatic
    fun getClientRequest(
        actions: SignalRWorkerAction = SignalRWorkerAction.connectPullCleanup()
    ): OneTimeWorkRequest {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val parameters = Data.Builder()
            .putInt(SignalRClientWorker.ACTION_ARG, actions.value)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SignalRClientWorker>()
            .setConstraints(constraints)
            .setInputData(parameters)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                DEFAULT_DELAY_BETWEEN_RETRY,
                DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT
            ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        return workRequest.build()
    }

    @JvmStatic
    fun WorkManager.generateFeed() {
        enqueueUniqueWork(
            GenerateNewsfeedWorker.UNIQUE_WORK_REQUEST_NAME,
            ExistingWorkPolicy.REPLACE,
            getGenerateFeedRequest()
        )
    }

    @JvmStatic
    fun WorkManager.downloadAttachment(messageId: Long) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val parameters = Data.Builder()
            .putLong(AttachmentDownloadWorker.MESSAGE_ID_ARG, messageId)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<AttachmentDownloadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .setInputData(parameters)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                DEFAULT_DELAY_BETWEEN_RETRY,
                DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT
            )
            .build()

        beginWith(downloadWorkRequest).then(getGenerateFeedRequest()).enqueue()
    }

    @JvmStatic
    fun WorkManager.downloadGroupData(messageId: Long) {
        val parameters = Data.Builder()
            .putLong(GroupDownloadWorker.MESSAGE_ID_ARG, messageId)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<GroupDownloadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(parameters)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                DEFAULT_DELAY_BETWEEN_RETRY,
                DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT
            )
            .build()
        enqueueUniqueWork(
            GroupDownloadWorker.getUniqueWorkName(messageId),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    @JvmStatic
    fun WorkManager.createOrUpdateGroup(
        groupName: String?, members: List<String>?,
        existingGroupAddress: String?, actionType: MessageType
    ) {
        val parameters = Data.Builder()
            .putString(GroupUploadWorker.GROUP_NAME_ARG, groupName)
            .putStringArray(GroupUploadWorker.MEMBERS_ARG, members?.toTypedArray() ?: arrayOf())
            .putString(GroupUploadWorker.EXISTING_GROUP_ADDRESS_ARG, existingGroupAddress)
            .putString(GroupUploadWorker.ACTION_TYPE_ARG, actionType.name)
            .build()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<GroupUploadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .setInputData(parameters)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                DEFAULT_DELAY_BETWEEN_RETRY,
                DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT
            )
            .build()
        enqueueUniqueWork(
            GroupUploadWorker.getUniqueWorkRequest(existingGroupAddress),
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
    }

    @JvmStatic
    fun WorkManager.sendMessage(
        messageId: Long,
        additionalDestinations: Set<String> = hashSetOf()
    ): UUID {
        val parameters = Data.Builder()
            .putLong(MessageSenderWorker.MESSAGE_ID_ARG, messageId)
            .putStringArray(
                MessageSenderWorker.ADDITIONAL_DESTINATIONS_ARG,
                additionalDestinations.toTypedArray()
            )
            .build()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<MessageSenderWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .setInputData(parameters)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                DEFAULT_DELAY_BETWEEN_RETRY,
                DEFAULT_DELAY_BETWEEN_REQUEST_TIME_UNIT
            )
            .build()
        enqueueUniqueWork(
            MessageSenderWorker.getUniqueWorkRequestName(messageId),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest.id
    }

    @JvmStatic
    fun WorkManager.schedulePeriodicClientSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val actions = SignalRWorkerAction.Connect() and SignalRWorkerAction.Pull()
        val parameters =
            Data.Builder().putInt(SignalRClientWorker.ACTION_ARG, actions.value).build()

        val signalRClientRequest = PeriodicWorkRequestBuilder<SignalRClientWorker>(
            SignalRClientWorker.PERIODIC_WORK_REPEAT_INTERVAL,
            SignalRClientWorker.PERIODIC_WORK_REQUEST_TIME_UNIT
        ).setConstraints(constraints)
            .setInputData(parameters)
            .build()

        enqueueUniquePeriodicWork(
            SignalRClientWorker.UNIQUE_PERIODIC_WORK_REQUEST,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            signalRClientRequest
        )
    }

    @JvmStatic
    fun WorkManager.invokeClient(
        actions: SignalRWorkerAction = SignalRWorkerAction.connectPullCleanup(),
    ) {
        enqueueUniqueWork(
            SignalRClientWorker.UNIQUE_ONE_TIME_REQUEST,
            ExistingWorkPolicy.REPLACE,
            getClientRequest(actions)
        )
    }

    @JvmStatic
    fun WorkManager.syncGraphAndInvokeClient(
        actions: SignalRWorkerAction = SignalRWorkerAction.connectPullCleanup()
    ) {
        beginWith(getSyncGraphRequest())
            .then(
                getClientRequest(
                    actions = actions
                )
            )
            .enqueue()
    }
}
