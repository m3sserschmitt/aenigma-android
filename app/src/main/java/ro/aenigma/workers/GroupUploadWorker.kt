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
import kotlinx.coroutines.flow.first
import ro.aenigma.R
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.GuardEntity
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toExportedData
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.GroupEntityFactory
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.GroupData
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.GroupDataExtensions.incrementNonce
import ro.aenigma.models.extensions.GroupDataExtensions.removeMembers
import ro.aenigma.models.extensions.GroupDataExtensions.withMembers
import ro.aenigma.models.extensions.GroupDataExtensions.withName
import ro.aenigma.models.factories.GroupDataFactory
import ro.aenigma.models.factories.ExportedContactDataFactory
import ro.aenigma.services.MessageSaver
import ro.aenigma.services.NotificationService
import ro.aenigma.util.SerializerExtensions.toCanonicalJson
import java.util.concurrent.TimeUnit

@HiltWorker
class GroupUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val messageSaver: MessageSaver,
    private val signatureService: SignatureService,
    private val notificationService: NotificationService
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORKER_NOTIFICATION_ID = 102
        private const val UNIQUE_WORK_NAME = "UploadGroupWorkRequest"
        private const val DELAY_BETWEEN_RETRIES: Long = 5
        private const val MAX_ATTEMPTS_COUNT = 5
        const val GROUP_NAME_ARG = "GroupName"
        const val MEMBERS_ARG = "Members"
        const val EXISTING_GROUP_ADDRESS_ARG = "GroupAddress"
        const val ACTION_TYPE_ARG = "Action"

        @JvmStatic
        fun createOrUpdateGroupWorkRequest(
            workManager: WorkManager, groupName: String?, members: List<String>?,
            existingGroupAddress: String?, actionType: MessageType
        ) {
            val parameters = Data.Builder()
                .putString(GROUP_NAME_ARG, groupName)
                .putStringArray(MEMBERS_ARG, members?.toTypedArray() ?: arrayOf())
                .putString(EXISTING_GROUP_ADDRESS_ARG, existingGroupAddress)
                .putString(ACTION_TYPE_ARG, actionType.name)
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
            workManager.enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
        }
    }

    private suspend fun createNewGroup(
        memberAddresses: List<String>,
        userName: String,
        groupName: String,
        admins: List<String>,
        guard: GuardEntity
    ): GroupData? {
        signatureService.publicKey ?: return null
        val contacts = memberAddresses.mapNotNull { item -> repository.local.getContact(item) }
            .filter { item -> item.type == ContactType.CONTACT }
        val members = contacts.filter { item ->
            item.name != null && item.publicKey != null && item.guardHostname != null && item.guardAddress != null
        }.map { item ->
            ExportedContactDataFactory.create(
                name = item.name.toString(),
                publicKey = item.publicKey.toString(),
                guardAddress = item.guardAddress.toString(),
                guardHostname = item.guardHostname.toString()
            )
        } + ExportedContactDataFactory.create(
            name = userName,
            publicKey = signatureService.publicKey.toString(),
            guardHostname = guard.hostname.toString(),
            guardAddress = guard.address.toString()
        )
        return GroupDataFactory.create(name = groupName, members = members, admins = admins)
    }

    private fun renameGroup(existingGroupData: GroupData, name: String): GroupData? {
        return existingGroupData.withName(name)?.incrementNonce()
    }

    private fun removeGroupMembers(
        existingGroupData: GroupData,
        memberAddresses: List<String>
    ): GroupData? {
        return existingGroupData.removeMembers(memberAddresses)?.incrementNonce()
    }

    private suspend fun addGroupMembers(
        existingGroupData: GroupData,
        memberAddresses: List<String>
    ): GroupData? {
        val members = existingGroupData.members?.toHashSet() ?: return null
        memberAddresses.forEach { address ->
            if (!members.contains(ExportedContactDataFactory.create(address))) {
                repository.local.getContact(address)?.let { c ->
                    c.toExportedData()?.let { ecd ->
                        members.add(ecd)
                    }
                }
            }
        }
        return existingGroupData.withMembers(members.toList())?.incrementNonce()
    }

    private suspend fun createGroupData(
        existingGroupData: GroupData?,
        groupName: String?,
        memberAddresses: List<String>?,
        userName: String?,
        admins: List<String>,
        actionType: MessageType,
        guard: GuardEntity,
    ): GroupData? {
        return when (actionType) {
            MessageType.GROUP_RENAMED -> {
                groupName ?: return null
                existingGroupData ?: return null
                renameGroup(existingGroupData, groupName)
            }

            MessageType.GROUP_CREATE -> {
                memberAddresses ?: return null
                if (memberAddresses.isEmpty()) return null
                userName ?: return null
                groupName ?: return null
                createNewGroup(memberAddresses, userName, groupName, admins, guard)
            }

            MessageType.GROUP_MEMBER_ADD -> {
                existingGroupData ?: return null
                memberAddresses ?: return null
                if (memberAddresses.isEmpty()) return null
                addGroupMembers(existingGroupData, memberAddresses)
            }

            MessageType.GROUP_MEMBER_REMOVE -> {
                memberAddresses ?: return null
                if (memberAddresses.isEmpty()) return null
                existingGroupData ?: return null
                removeGroupMembers(existingGroupData, memberAddresses)
            }

            else -> null
        }
    }

    private suspend fun saveGroupEntity(groupData: GroupData, resourceUrl: String) {
        groupData.name ?: return
        groupData.address ?: return
        val contact = ContactEntityFactory.createGroup(
            address = groupData.address,
            name = groupData.name
        )
        repository.local.insertOrUpdateContact(contact)
        val group = GroupEntityFactory.create(
            address = groupData.address,
            groupData = groupData,
            resourceUrl = resourceUrl
        )
        repository.local.insertOrUpdateGroup(group)
    }

    private suspend fun sendGroupUpdate(
        groupData: GroupData,
        key: ByteArray,
        actionType: MessageType,
        additionalDestinations: Set<String> = hashSetOf()
    ): Boolean {
        groupData.address ?: return false
        val message = MessageEntityFactory.createOutgoing(
            chatId = groupData.address,
            text = CryptoProvider.masterKeyEncryptEx(key),
            type = actionType,
            actionFor = null
        )
        return messageSaver.saveOutgoingMessage(
            message,
            additionalDestinations = additionalDestinations
        )
    }

    override suspend fun doWork(): Result {
        if (signatureService.address == null || signatureService.publicKey == null || runAttemptCount >= MAX_ATTEMPTS_COUNT) {
            return Result.failure()
        }
        val actionTypeString = inputData.getString(ACTION_TYPE_ARG) ?: return Result.failure()
        val userName = repository.local.name.first()
        val actionType = MessageType.valueOf(actionTypeString)
        val groupName = inputData.getString(GROUP_NAME_ARG)
        val memberAddresses = inputData.getStringArray(MEMBERS_ARG)?.toList()
        val groupAddress = inputData.getString(EXISTING_GROUP_ADDRESS_ARG)
        val admins = listOf(signatureService.address!!)
        val existingGroupData =
            if (groupAddress != null) repository.local.getContactWithGroup(groupAddress)?.group?.groupData else null
        val guard = repository.local.getGuard() ?: return Result.failure()

        val groupData = createGroupData(
            existingGroupData = existingGroupData,
            groupName = groupName,
            memberAddresses = memberAddresses,
            userName = userName,
            admins = admins,
            actionType = actionType,
            guard = guard
        ) ?: return Result.failure()
        groupData.members ?: return Result.failure()
        val serializedGroupData = groupData.toCanonicalJson()?.toByteArray()
            ?: return Result.failure()
        val encryptionDto = CryptoProvider.encrypt(serializedGroupData) ?: return Result.failure()
        val destinations = groupData.members.mapNotNull { item -> item.address }.toHashSet()
            .union(existingGroupData?.members?.mapNotNull { item -> item.address } ?: hashSetOf())
        val accessCount = (destinations.count() - 1) * GroupDownloadWorker.MAX_RETRY_COUNT
        val response = repository.remote.createSharedData(encryptionDto.encryptedData, accessCount)
            ?: return Result.retry()
        response.resourceUrl ?: return Result.retry()

        saveGroupEntity(groupData, response.resourceUrl)
        sendGroupUpdate(groupData, encryptionDto.key, actionType, destinations)

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            WORKER_NOTIFICATION_ID,
            notificationService.createWorkerNotification(applicationContext.getString(R.string.uploading_channel_info))
        )
    }
}
