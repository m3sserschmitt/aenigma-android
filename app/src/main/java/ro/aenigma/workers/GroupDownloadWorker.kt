package ro.aenigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.R
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withName
import ro.aenigma.data.database.extensions.ContactEntityExtensions.withNewMessage
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.GroupEntityFactory
import ro.aenigma.models.GroupData
import ro.aenigma.services.NotificationService
import java.util.concurrent.TimeUnit

@HiltWorker
class GroupDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val signatureService: SignatureService,
    private val notificationService: NotificationService
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORKER_NOTIFICATION_ID = 101
        private const val UNIQUE_WORK_REQUEST_NAME = "GroupDownloadWorkRequest"
        private const val DELAY_BETWEEN_RETRIES: Long = 5
        private const val MESSAGE_ID_ARG = "MessageId"
        const val MAX_RETRY_COUNT = 5

        @JvmStatic
        fun createWorkRequest(workManager: WorkManager, messageId: Long) {
            val parameters = Data.Builder()
                .putLong(MESSAGE_ID_ARG, messageId)
                .build()
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<GroupDownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(
                "$UNIQUE_WORK_REQUEST_NAME-$messageId", ExistingWorkPolicy.KEEP, workRequest
            )
        }
    }

    private suspend fun createContactEntities(groupData: GroupData, resourceUrl: String) {
        groupData.address ?: return
        val contact = (repository.local.getContactWithGroup(groupData.address)?.contact
            ?: ContactEntityFactory.createGroup(
                address = groupData.address,
                name = groupData.name,
            )).withName(groupData.name).withNewMessage() ?: return
        val group = GroupEntityFactory.create(
            address = groupData.address,
            groupData = groupData,
            resourceUrl = resourceUrl
        )
        repository.local.insertOrUpdateContact(contact)
        repository.local.insertOrUpdateGroup(group)

        groupData.members ?: return

        for (member in groupData.members) {
            if (member.name == null || member.address == null || signatureService.address == member.address
                || !member.publicKey.isValidPublicKey()
            ) {
                continue
            }
            val c = ContactEntityFactory.createContact(
                address = member.address,
                name = member.name,
                publicKey = member.publicKey,
                guardHostname = member.guardHostname,
                guardAddress = member.guardAddress,
            )
            repository.local.insertOrIgnoreContact(c)
        }
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            return Result.failure()
        }
        val messageId = inputData.getLong(MESSAGE_ID_ARG, Long.MIN_VALUE)
        if (messageId < 0) {
            return Result.failure()
        }
        val message = if (messageId > 0)
            repository.local.getMessageWithAttachments(messageId)
        else return Result.failure()
        message?.attachment?.url ?: return Result.failure()
        message.attachment.passphrase ?: return Result.failure()
        message.message.senderAddress ?: return Result.failure()

        val existentGroup = repository.local.getContactsWithGroup().firstOrNull { item ->
            item.group?.groupData?.address == message.message.chatId
        }?.group?.groupData
        val groupData = repository.remote.getGroupDataByUrl(
            url = message.attachment.url,
            existentGroup = existentGroup,
            passphrase = CryptoProvider.base64Decode(message.attachment.passphrase)
                ?: return Result.failure(),
            expectedPublisherAddress = message.message.senderAddress
        ) ?: return Result.retry()
        createContactEntities(groupData, message.attachment.url)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            WORKER_NOTIFICATION_ID,
            notificationService.createWorkerNotification(applicationContext.getString(R.string.downloading_channel_info))
        )
    }
}
