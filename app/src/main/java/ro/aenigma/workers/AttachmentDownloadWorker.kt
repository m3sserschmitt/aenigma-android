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
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.data.Repository
import ro.aenigma.models.AttachmentDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithAttachmentsDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.services.Notifier
import ro.aenigma.util.ContextExtensions.getCacheFile
import ro.aenigma.util.ContextExtensions.createTempCacheFile
import ro.aenigma.util.ContextExtensions.extractZip
import ro.aenigma.util.ContextExtensions.toContentUri
import java.io.File

@HiltWorker
class AttachmentDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {

    companion object {
        private const val UNIQUE_WORK_REQUEST_NAME = "attachment-download-worker"
        const val MESSAGE_ID_ARG = "message-id"
        const val MAX_RETRY_COUNT = 3

        fun getUniqueWorkName(messageId: Long): String {
            return "$UNIQUE_WORK_REQUEST_NAME-$messageId"
        }
    }

    suspend fun downloadArchive(attachment: AttachmentDto): File? {
        val url = attachment.url ?: return null
        val key = CryptoProvider.base64Decode(attachment.passphrase ?: return null) ?: return null
        val archive = applicationContext.createTempCacheFile(null)
        return if (repository.remote.getEncryptedFile(url, key, archive)) {
            archive
        } else {
            archive.delete()
            null
        }
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
        val archive = if (!attachment.path.isNullOrBlank()) {
            val cachedArchive = applicationContext.getCacheFile(attachment.path)
            if (!cachedArchive.exists()) {
                downloadArchive(attachment)
            } else {
                cachedArchive
            }
        } else {
            downloadArchive(attachment)
        } ?: return null

        storeAttachment(message, archive)
        return archive
    }

    private suspend fun incrementFileAccessCount(messageWithAttachments: MessageWithAttachmentsDto) {
        if (!messageWithAttachments.attachment?.url.isNullOrBlank()) {
            repository.remote.incrementFileAccessCount(messageWithAttachments.attachment.url)
        }
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

            if (message.message.type != MessageType.FILES) return Result.success()
            if (message.message.files?.isNotEmpty() == true) return Result.success()

            setForeground(getForegroundInfo())

            val archive = resolveAttachments(message) ?: return Result.retry()
            val files = applicationContext.extractZip(
                archive,
                message.message.chatId
            )
            resolveFiles(message.message, files)
            incrementFileAccessCount(message)
            archive.delete()

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ForegroundInfo(
            id.hashCode(),
            notifier.createWorkerNotification(applicationContext.getString(R.string.downloading_file)),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        ) else
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.downloading_file))
            )
    }
}
