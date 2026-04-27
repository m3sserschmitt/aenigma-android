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
import kotlinx.coroutines.flow.first
import ro.aenigma.R
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.models.AttachmentDto
import ro.aenigma.models.GroupDataDto
import ro.aenigma.models.GroupDto
import ro.aenigma.models.GuardDto
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.ContactDtoExtensions.toExportedContactDataDto
import ro.aenigma.models.extensions.GroupDataExtensions.incrementNonce
import ro.aenigma.models.extensions.GroupDataExtensions.removeMembers
import ro.aenigma.models.extensions.GroupDataExtensions.withMembers
import ro.aenigma.models.extensions.GroupDataExtensions.withName
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.models.factories.GroupDataFactory
import ro.aenigma.models.factories.ExportedContactDataFactory
import ro.aenigma.models.factories.MessageDtoFactory
import ro.aenigma.services.MessageSaver
import ro.aenigma.services.Notifier
import ro.aenigma.util.Constants.Companion.ENCRYPTION_KEY_SIZE
import ro.aenigma.util.Constants.Companion.GROUP_UPLOAD_NOTIFICATION_ID
import ro.aenigma.util.SerializerExtensions.toCanonicalJson

@HiltWorker
class GroupUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val messageSaver: MessageSaver,
    private val signatureService: SignatureService,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "upload-group-worker"
        private const val MAX_ATTEMPTS_COUNT = 3
        const val GROUP_NAME_ARG = "group-name"
        const val MEMBERS_ARG = "members"
        const val EXISTING_GROUP_ADDRESS_ARG = "group-address"
        const val ACTION_TYPE_ARG = "action"

        @JvmStatic
        fun getUniqueWorkRequest(groupAddress: String?): String {
            return if (groupAddress.isNullOrBlank()) {
                UNIQUE_WORK_NAME
            } else {
                "$UNIQUE_WORK_NAME-$groupAddress"
            }
        }
    }

    private suspend fun createNewGroup(
        memberAddresses: List<String>,
        userName: String,
        groupName: String,
        admins: List<String>,
        guard: GuardDto
    ): GroupDataDto? {
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
            guardHostname = guard.hostname,
            guardAddress = guard.address
        )
        return GroupDataFactory.create(name = groupName, members = members, admins = admins)
    }

    private fun renameGroup(existingGroupDataDto: GroupDataDto, name: String): GroupDataDto {
        return existingGroupDataDto.withName(name).incrementNonce()
    }

    private fun removeGroupMembers(
        existingGroupDataDto: GroupDataDto,
        memberAddresses: List<String>
    ): GroupDataDto? {
        if (memberAddresses.contains(signatureService.address)) {
            return null
        }
        return existingGroupDataDto.removeMembers(memberAddresses).incrementNonce()
    }

    private suspend fun addGroupMembers(
        existingGroupDataDto: GroupDataDto,
        memberAddresses: List<String>
    ): GroupDataDto? {
        val members = existingGroupDataDto.members?.toHashSet() ?: return null
        memberAddresses.forEach { address ->
            if (!members.contains(ExportedContactDataFactory.create(address))) {
                repository.local.getContact(address)?.let { c ->
                    c.toExportedContactDataDto().let { ecd ->
                        members.add(ecd)
                    }
                }
            }
        }
        return existingGroupDataDto.withMembers(members.toList()).incrementNonce()
    }

    private suspend fun createGroupData(
        existingGroupDataDto: GroupDataDto?,
        groupName: String?,
        memberAddresses: List<String>?,
        userName: String?,
        admins: List<String>,
        actionType: MessageType,
        guard: GuardDto,
    ): GroupDataDto? {
        return when (actionType) {
            MessageType.GROUP_RENAMED -> {
                groupName ?: return null
                existingGroupDataDto ?: return null
                renameGroup(existingGroupDataDto, groupName)
            }

            MessageType.GROUP_CREATE -> {
                memberAddresses ?: return null
                if (memberAddresses.isEmpty()) return null
                userName ?: return null
                groupName ?: return null
                createNewGroup(memberAddresses, userName, groupName, admins, guard)
            }

            MessageType.GROUP_MEMBER_ADD -> {
                existingGroupDataDto ?: return null
                memberAddresses ?: return null
                if (memberAddresses.isEmpty()) return null
                addGroupMembers(existingGroupDataDto, memberAddresses)
            }

            MessageType.GROUP_MEMBER_REMOVE -> {
                memberAddresses ?: return null
                if (memberAddresses.isEmpty()) return null
                existingGroupDataDto ?: return null
                removeGroupMembers(existingGroupDataDto, memberAddresses)
            }

            else -> null
        }
    }

    private suspend fun saveGroupEntity(groupDataDto: GroupDataDto, resourceUrl: String) {
        groupDataDto.name ?: return
        groupDataDto.address ?: return
        val contact = ContactDtoFactory.createGroup(
            address = groupDataDto.address,
            name = groupDataDto.name
        )
        repository.local.insertOrUpdateContact(contact)
        val group = GroupDto(
            address = groupDataDto.address,
            groupData = groupDataDto,
            resourceUrl = resourceUrl
        )
        repository.local.insertOrUpdateGroup(group)
    }

    private suspend fun sendGroupUpdate(
        groupDataDto: GroupDataDto,
        key: ByteArray,
        resourceUrl: String,
        actionType: MessageType,
        additionalDestinations: Set<String> = hashSetOf()
    ): Boolean {
        groupDataDto.address ?: return false
        val encodedKey = CryptoProvider.base64Encode(key)
        return messageSaver.saveOutgoingMessage(
            MessageDtoFactory.createOutgoing(
                chatId = groupDataDto.address,
                text = encodedKey, // preserve compatibility with version 1.0.1; it will be set
                // to null in the future;
                type = actionType,
                actionFor = null
            ),
            additionalDestinations = additionalDestinations,
            AttachmentDto(
                messageId = 0,
                path = null,
                passphrase = encodedKey,
                url = resourceUrl
            )
        )
    }

    private fun calculateDestinations(
        groupData: GroupDataDto?,
        existingGroupData: GroupDataDto?
    ): Set<String> {
        return groupData?.members?.mapNotNull { item -> item.address }
            ?.union(existingGroupData?.members?.mapNotNull { item -> item.address } ?: listOf())
            ?.filter { item -> item != signatureService.address }
            ?.toSet() ?: setOf()
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
            existingGroupDataDto = existingGroupData,
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
        val destinations = calculateDestinations(groupData, existingGroupData)
        val destinationsCount = destinations.count()
        if (destinationsCount <= 0) {
            return Result.failure()
        }
        val key = CryptoProvider.generateRandomBytes(ENCRYPTION_KEY_SIZE)
        val response =
            repository.remote.createSharedData(serializedGroupData, key, destinationsCount)
                ?: return Result.retry()
        response.resourceUrl ?: return Result.retry()

        saveGroupEntity(groupData, response.resourceUrl)
        sendGroupUpdate(groupData, key, response.resourceUrl, actionType, destinations)

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(
                GROUP_UPLOAD_NOTIFICATION_ID,
                notifier.createWorkerNotification(applicationContext.getString(R.string.uploading_channel_info)),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            ) else
            ForegroundInfo(
                GROUP_UPLOAD_NOTIFICATION_ID,
                notifier.createWorkerNotification(applicationContext.getString(R.string.uploading_channel_info))
            )
    }
}
