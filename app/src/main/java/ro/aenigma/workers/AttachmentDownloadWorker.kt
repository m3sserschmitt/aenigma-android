package ro.aenigma.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
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
import ro.aenigma.data.Repository
import ro.aenigma.models.AttachmentDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithAttachmentsDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.services.NotificationService
import ro.aenigma.util.Constants.Companion.ATTACHMENT_DOWNLOAD_NOTIFICATION_ID
import ro.aenigma.util.ContextExtensions.getCacheFile
import ro.aenigma.util.ContextExtensions.createTempCacheFile
import ro.aenigma.util.ContextExtensions.extractZip
import ro.aenigma.util.ContextExtensions.toContentUri
import java.io.File
import java.util.concurrent.TimeUnit

@HiltWorker
class AttachmentDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, params) {

    companion object {
        private const val UNIQUE_WORK_REQUEST_NAME = "attachment-download-worker"
        private const val MESSAGE_ID_ARG = "message-id"
        const val MAX_RETRY_COUNT = 5
        private const val DELAY_BETWEEN_RETRIES: Long = 5

        fun createRequest(workManager: WorkManager, messageId: Long) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val parameters = Data.Builder()
                .putLong(MESSAGE_ID_ARG, messageId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AttachmentDownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(
                getUniqueWorkName(messageId), ExistingWorkPolicy.KEEP, workRequest
            )
        }

        fun getUniqueWorkName(messageId: Long): String {
            return "$UNIQUE_WORK_REQUEST_NAME-$messageId"
        }
    }

    private suspend fun downloadEncryptedFile(url: String): File? {
        val tempFile = applicationContext.createTempCacheFile()
        if (repository.remote.getFile(url, tempFile)) {
            repository.remote.incrementFileAccessCount(url)
            return tempFile
        } else {
            return null
        }
    }

    private fun decryptArchive(file: File, passphrase: String?): File? {
        val key = CryptoProvider.base64Decode(passphrase ?: return null) ?: return null
        return CryptoProvider.decrypt(file, key)
    }

    private suspend fun storeAttachment(message: MessageWithAttachmentsDto, archive: File) {
        repository.local.insertOrUpdateAttachment(
            AttachmentDto(
                messageId = message.message.id,
                path = archive.name,
                url = message.attachment?.url,
                passphrase = message.attachment?.passphrase
            )
        )
    }

    private suspend fun resolveAttachments(message: MessageWithAttachmentsDto): File? {
        val attachment = message.attachment ?: return null
        val archive = if (attachment.path.isNullOrBlank()) {
            val downloaded = downloadEncryptedFile(attachment.url ?: return null) ?: return null
            val decrypted = decryptArchive(downloaded, attachment.passphrase) ?: return null
            downloaded.delete()
            decrypted
        } else {
            applicationContext.getCacheFile(attachment.path)
        }

        storeAttachment(message, archive)
        return archive
    }

    private suspend fun resolveFiles(
        message: MessageDto,
        files: List<File>
    ) {
        repository.local.updateMessage(
            message.copy(files = files.map { file ->
                applicationContext.toContentUri(file).toString()
            })
        )
    }

    override suspend fun doWork(): Result {
        return try {
            if (runAttemptCount >= MAX_RETRY_COUNT) return Result.failure()

            val messageId = inputData.getLong(MESSAGE_ID_ARG, Long.MIN_VALUE)
            val message = repository.local.getMessageWithAttachments(messageId.takeIf { it > 0 }
                ?: return Result.failure()).takeIf { !it?.message?.senderAddress.isNullOrBlank() }
                ?: return Result.failure()

            if (message.message.type != MessageType.FILES) return Result.failure()
            if (message.message.files?.isNotEmpty() == true) return Result.success()

            setForeground(getForegroundInfo())

            val archive = resolveAttachments(message) ?: return Result.retry()
            val files = applicationContext.extractZip(
                archive,
                message.message.chatId
            )
            resolveFiles(message.message, files)

            archive.delete()

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ForegroundInfo(
            ATTACHMENT_DOWNLOAD_NOTIFICATION_ID,
            notificationService.createWorkerNotification(applicationContext.getString(R.string.downloading_file)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                ATTACHMENT_DOWNLOAD_NOTIFICATION_ID,
                notificationService.createWorkerNotification(applicationContext.getString(R.string.downloading_file))
            )
    }
}
