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
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.HashExtensions.getSha256
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.GroupEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.GroupData
import ro.aenigma.models.GroupMember
import ro.aenigma.models.MessageAction
import ro.aenigma.models.enums.ContactType
import ro.aenigma.services.MessageSaver
import ro.aenigma.util.MessageActionType
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class GroupUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val messageSaver: MessageSaver,
    private val signatureService: SignatureService
) : CoroutineWorker(context, params) {

    companion object {
        private const val UNIQUE_WORK_NAME = "UploadGroupWorkRequest"
        private const val DELAY_BETWEEN_RETRIES: Long = 3
        private const val MAX_ATTEMPTS_COUNT = 3
        const val GROUP_NAME_ARG = "GroupName"
        const val USER_NAME_ARG = "AdminName"
        const val MEMBERS_ARG = "Members"
        const val EXISTING_GROUP_ADDRESS_ARG = "GroupAddress"

        @JvmStatic
        fun createOrUpdateGroupWorkRequest(
            workManager: WorkManager, groupName: String, adminName: String, members: List<String>,
            existingGroupAddress: String?
        ) {
            val parameters = Data.Builder()
                .putString(GROUP_NAME_ARG, groupName)
                .putString(USER_NAME_ARG, adminName)
                .putStringArray(MEMBERS_ARG, members.toTypedArray())
                .putString(EXISTING_GROUP_ADDRESS_ARG, existingGroupAddress)
                .build()
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<GroupUploadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(parameters)
                .setBackoffCriteria(BackoffPolicy.LINEAR, DELAY_BETWEEN_RETRIES, TimeUnit.SECONDS)
                .build()
            val workName =
                if (existingGroupAddress != null) "$UNIQUE_WORK_NAME-$existingGroupAddress" else UNIQUE_WORK_NAME
            val existingWorkPolicy = if(existingGroupAddress != null) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.APPEND_OR_REPLACE
            workManager.enqueueUniqueWork(
                workName,
                existingWorkPolicy,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        if (signatureService.address == null || signatureService.publicKey == null || runAttemptCount >= MAX_ATTEMPTS_COUNT) {
            return Result.failure()
        }
        val groupName = inputData.getString(GROUP_NAME_ARG) ?: return Result.failure()
        val userName = inputData.getString(USER_NAME_ARG) ?: return Result.failure()
        val memberAddresses = inputData.getStringArray(MEMBERS_ARG) ?: return Result.failure()
        val groupAddress =
            inputData.getString(EXISTING_GROUP_ADDRESS_ARG) ?: UUID.randomUUID().toString()
                .getSha256()
        val contacts = memberAddresses.mapNotNull { item -> repository.local.getContact(item) }
            .filter { item -> item.type == ContactType.CONTACT }
        val members = contacts.map { item -> GroupMember(item.name, item.publicKey) } +
                GroupMember(userName, signatureService.publicKey)
        val groupData = GroupData(groupAddress, groupName, members)
        val serializedGroupData = Gson().toJson(groupData).toByteArray()
        val encryptedGroupData = contacts.mapNotNull { item ->
            CryptoProvider.encryptEx(item.publicKey, serializedGroupData)
        } + CryptoProvider.encryptEx(signatureService.publicKey, serializedGroupData)
        if (encryptedGroupData.isEmpty()) {
            return Result.failure()
        }
        val serializedEncryptedGroupData = Gson().toJson(encryptedGroupData)
        val response = repository.remote.createSharedData(serializedEncryptedGroupData) ?: return Result.retry()
        val contact = ContactEntity(
            address = groupAddress,
            name = groupName,
            publicKey = "",
            guardHostname = "",
            guardAddress = "",
            type = ContactType.GROUP,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )
        repository.local.insertOrUpdateContact(contact)
        val group = GroupEntity(groupAddress, groupData, response.resourceUrl!!)
        repository.local.insertOrUpdateGroup(group)
        val message = MessageEntity(
            chatId = groupAddress,
            text = "",
            incoming = false,
            uuid = null,
            action = MessageAction(MessageActionType.GROUP_UPDATE, null, signatureService.address)
        )
        messageSaver.saveOutgoingMessage(message, userName)
        return Result.success()
    }
}
