package ro.aenigma.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.crypto.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.RemoteDataSource
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.GroupEntity
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.models.GroupData
import ro.aenigma.models.enums.ContactType
import ro.aenigma.util.getTagQueryParameter
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class GroupDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val signatureService: SignatureService
) : CoroutineWorker(context, params) {

    companion object {
        private const val UNIQUE_WORK_REQUEST_NAME = "GroupDownloadWorkRequest"
        private const val DELAY_BETWEEN_RETRIES: Long = 3
        const val GROUP_RESOURCE_URL = "GroupResourceUrl"
        const val MAX_RETRY_COUNT = 2

        @JvmStatic
        fun createWorkRequest(workManager: WorkManager, resourceUrl: String) {
            val parameters = Data.Builder()
                .putString(GROUP_RESOURCE_URL, resourceUrl)
                .build()
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<GroupDownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()
            val tag = resourceUrl.getTagQueryParameter()
            workManager.enqueueUniqueWork(
                "$UNIQUE_WORK_REQUEST_NAME-$tag", ExistingWorkPolicy.KEEP, workRequest
            )
        }
    }

    private suspend fun createContactEntities(groupData: GroupData, resourceUrl: String) {
        val contact = ContactEntity(
            address = groupData.address!!,
            name = groupData.name!!,
            publicKey = "",
            guardHostname = null,
            guardAddress = "",
            type = ContactType.GROUP,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )
        val group = GroupEntity(
            address = groupData.address,
            groupData = groupData,
            resourceUrl = resourceUrl
        )
        repository.local.insertOrUpdateContact(contact)
        repository.local.insertOrUpdateGroup(group)

        groupData.members ?: return

        for (member in groupData.members) {
            val address = member.publicKey.getAddressFromPublicKey()
            if (member.name == null || address == null || signatureService.address == address
                || !member.publicKey.isValidPublicKey()
            ) {
                continue
            }
            val c = ContactEntity(
                address = address,
                name = member.name,
                publicKey = member.publicKey!!,
                guardHostname = null,
                guardAddress = "",
                type = ContactType.CONTACT,
                hasNewMessage = false,
                lastSynchronized = ZonedDateTime.now()
            )
            repository.local.insertOrIgnoreContact(c)
        }
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount > MAX_RETRY_COUNT) {
            return Result.failure()
        }
        val resourceUrl = inputData.getString(GROUP_RESOURCE_URL) ?: return Result.failure()
        val api = EnigmaApi.initApi(resourceUrl)
        val tag = resourceUrl.getTagQueryParameter() ?: return Result.failure()
        val groupData =
            RemoteDataSource(api, signatureService).getGroupData(tag) ?: return Result.failure()
        createContactEntities(groupData, resourceUrl)
        return Result.success()
    }
}
