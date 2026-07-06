/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.models.GroupDataDto
import ro.aenigma.models.GroupDto
import ro.aenigma.models.extensions.ContactDtoExtensions.withName
import ro.aenigma.models.extensions.ContactDtoExtensions.withNewMessage
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.services.Notifier

@HiltWorker
class GroupDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
    private val signatureService: SignatureService,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_REQUEST_NAME = "group-download-worker"
        const val MESSAGE_ID_ARG = "message-id"
        const val MAX_RETRY_COUNT = 3

        fun getUniqueWorkName(messageId: Long): String {
            return "${UNIQUE_WORK_REQUEST_NAME}-$messageId"
        }
    }

    private suspend fun createContactEntities(groupDataDto: GroupDataDto, resourceUrl: String) {
        groupDataDto.address ?: return
        val contact = (repository.local.getContactWithGroup(groupDataDto.address)?.contact
            ?: ContactDtoFactory.createGroup(
                address = groupDataDto.address,
                name = groupDataDto.name,
            )).withName(groupDataDto.name).withNewMessage()
        val group = GroupDto(
            address = groupDataDto.address,
            groupData = groupDataDto,
            resourceUrl = resourceUrl
        )
        repository.local.insertOrUpdateContact(contact)
        repository.local.insertOrUpdateGroup(group)

        groupDataDto.members ?: return

        for (member in groupDataDto.members) {
            if (member.name == null || member.address == null || signatureService.address == member.address
                || !member.publicKey.isValidPublicKey()
            ) {
                continue
            }
            val c = ContactDtoFactory.createContact(
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
        val passphrase = message.attachment.passphrase ?: return Result.failure()
        message.message.senderAddress ?: return Result.failure()

        val existentGroup =
            repository.local.getContactWithGroup(message.message.chatId)?.group?.groupData
        val groupData = repository.remote.getGroupData(
            url = message.attachment.url,
            existentGroup = existentGroup,
            key = CryptoProvider.base64Decode(passphrase) ?: return Result.failure(),
            expectedPublisherAddress = message.message.senderAddress
        ) ?: return Result.retry()
        repository.remote.incrementFileAccessCount(message.attachment.url)
        createContactEntities(groupData, message.attachment.url)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.downloading_channel_info)),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            ) else
            ForegroundInfo(
                id.hashCode(),
                notifier.createWorkerNotification(applicationContext.getString(R.string.downloading_channel_info))
            )
    }
}
