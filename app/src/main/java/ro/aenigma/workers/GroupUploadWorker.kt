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
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.GroupEntityFactory
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.GroupData
import ro.aenigma.models.GroupMember
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.GroupDataExtensions.withMembers
import ro.aenigma.models.extensions.GroupDataExtensions.withName
import ro.aenigma.models.factories.GroupDataFactory
import ro.aenigma.models.factories.GroupMemberFactory
import ro.aenigma.services.MessageSaver
import ro.aenigma.util.SerializerExtensions.toJson
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
        const val USER_NAME_ARG = "UserName"
        const val MEMBERS_ARG = "Members"
        const val EXISTING_GROUP_ADDRESS_ARG = "GroupAddress"
        const val ACTION_TYPE_ARG = "Action"

        @JvmStatic
        fun createOrUpdateGroupWorkRequest(
            workManager: WorkManager, userName: String, groupName: String?, members: List<String>?,
            existingGroupAddress: String?, actionType: MessageType
        ) {
            val parameters = Data.Builder()
                .putString(GROUP_NAME_ARG, groupName)
                .putString(USER_NAME_ARG, userName)
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
            val existingWorkPolicy =
                if (existingGroupAddress != null) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.APPEND_OR_REPLACE
            workManager.enqueueUniqueWork(
                workName,
                existingWorkPolicy,
                workRequest
            )
        }
    }

    private suspend fun createNewGroup(
        memberAddresses: List<String>, userName: String, groupName: String, admins: List<String>
    ): GroupData? {
        signatureService.publicKey ?: return null
        val contacts = memberAddresses.mapNotNull { item -> repository.local.getContact(item) }
            .filter { item -> item.type == ContactType.CONTACT }
        val members = contacts.mapNotNull { item ->
            item.name?.let { name ->
                item.publicKey?.let { publicKey ->
                    GroupMemberFactory.create(name = name, publicKey = publicKey)
                }
            }
        } + GroupMemberFactory.create(userName, signatureService.publicKey!!)
        return GroupDataFactory.create(name = groupName, members = members, admins = admins)
    }

    private fun renameGroup(existingGroupData: GroupData, name: String): GroupData? {
        return existingGroupData.withName(name)
    }

    private fun leaveGroup(existingGroupData: GroupData): GroupData? {
        val members = existingGroupData.members?.filter { item ->
            item.address != signatureService.address
        }
        return existingGroupData.withMembers(members)
    }

    private suspend fun modifyGroupMembers(
        existingGroupData: GroupData, memberAddresses: List<String>, actionType: MessageType
    ): GroupData? {
        if (existingGroupData.members == null) {
            return null
        }

        val members = when (actionType) {
            MessageType.GROUP_MEMBER_REMOVE -> {
                val memberAddressesSet = HashSet(memberAddresses)
                existingGroupData.members.filter { item ->
                    !memberAddressesSet.contains(item.address)
                }
            }

            MessageType.GROUP_MEMBER_ADD -> {
                val result = mutableSetOf<GroupMember>()
                memberAddresses.mapNotNullTo(result) { address ->
                    val contact = repository.local.getContact(address)
                    contact?.name?.let { name ->
                        contact.publicKey?.let { publicKey ->
                            GroupMemberFactory.create(name, publicKey)
                        }
                    }
                }
                result.addAll(existingGroupData.members)
                result.toList()
            }

            else -> {
                existingGroupData.members
            }
        }
        return existingGroupData.withMembers(members)
    }

    private suspend fun createGroupData(
        existingGroupData: GroupData?,
        groupName: String?,
        memberAddresses: List<String>,
        userName: String?,
        admins: List<String>,
        actionType: MessageType
    ): GroupData? {
        return when (actionType) {
            MessageType.GROUP_RENAMED -> {
                groupName ?: return null
                existingGroupData ?: return null
                renameGroup(existingGroupData, groupName)
            }

            MessageType.GROUP_CREATE -> {
                if (memberAddresses.isEmpty()) return null
                userName ?: return null
                groupName ?: return null
                createNewGroup(memberAddresses, userName, groupName, admins)
            }

            MessageType.GROUP_MEMBER_LEFT -> {
                existingGroupData ?: return null
                leaveGroup(existingGroupData)
            }

            MessageType.GROUP_MEMBER_ADD,
            MessageType.GROUP_MEMBER_REMOVE -> {
                if (memberAddresses.isEmpty()) return null
                existingGroupData ?: return null
                modifyGroupMembers(existingGroupData, memberAddresses, actionType)
            }

            else -> null
        }
    }

    private fun encryptGroupData(
        groupData: GroupData,
        membersToEncryptFor: List<GroupMember>
    ): String? {
        val serializedGroupData = groupData.toJson()?.toByteArray() ?: return null
        val encryptedGroupData = membersToEncryptFor.mapNotNull { item ->
            item.publicKey?.let { publicKey ->
                CryptoProvider.encryptEx(publicKey, serializedGroupData)
            }
        }
        if (encryptedGroupData.isEmpty()) {
            return null
        }
        return encryptedGroupData.toJson()
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

    private suspend fun saveMessageEntity(
        groupData: GroupData,
        userName: String,
        memberAddresses: List<String>,
        resourceUrl: String,
        actionType: MessageType
    ) {
        groupData.address ?: return
        val message = MessageEntityFactory.createOutgoing(
            chatId = groupData.address,
            text = null,
            type = actionType,
            actionFor = null
        )
        messageSaver.saveOutgoingMessage(message, userName)
        if (actionType == MessageType.GROUP_MEMBER_REMOVE) {
            for (memberAddress in memberAddresses) {
                val m = MessageEntityFactory.createOutgoing(
                    chatId = memberAddress,
                    text = null,
                    type = actionType,
                    actionFor = null
                )
                messageSaver.saveOutgoingMessage(m, userName, resourceUrl)
            }
        }
    }

    override suspend fun doWork(): Result {
        if (signatureService.address == null || signatureService.publicKey == null || runAttemptCount >= MAX_ATTEMPTS_COUNT) {
            return Result.failure()
        }

        val actionTypeString = inputData.getString(ACTION_TYPE_ARG) ?: return Result.failure()
        val userName = inputData.getString(USER_NAME_ARG) ?: return Result.failure()
        val actionType = MessageType.valueOf(actionTypeString)
        val groupName = inputData.getString(GROUP_NAME_ARG)
        val memberAddresses = inputData.getStringArray(MEMBERS_ARG)?.toList() ?: listOf()
        val groupAddress = inputData.getString(EXISTING_GROUP_ADDRESS_ARG)
        val admins = listOf(signatureService.address!!)
        val existingGroupData =
            if (groupAddress != null) repository.local.getContactWithGroup(groupAddress)?.group?.groupData else null
        val groupData =
            createGroupData(
                existingGroupData,
                groupName,
                memberAddresses,
                userName,
                admins,
                actionType
            )
                ?: return Result.failure()
        groupData.members ?: return Result.failure()
        val membersToEncryptFor =
            if (groupData.members.size >= (existingGroupData?.members?.size
                    ?: 0)
            ) groupData.members else existingGroupData?.members
        membersToEncryptFor ?: return Result.failure()
        val encryptedGroupData = encryptGroupData(groupData, membersToEncryptFor)
            ?: return Result.failure()
        val response =
            repository.remote.createSharedData(encryptedGroupData, membersToEncryptFor.size - 1)
                ?: return Result.retry()
        response.resourceUrl ?: return Result.failure()
        saveGroupEntity(groupData, response.resourceUrl)
        saveMessageEntity(groupData, userName, memberAddresses, response.resourceUrl, actionType)

        return Result.success()
    }
}
