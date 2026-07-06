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

package ro.aenigma.models.extensions

import android.content.Context
import ro.aenigma.R
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.ArtifactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.MessageTypeExtensions.isGroupCreateOrUpdate
import ro.aenigma.models.extensions.MessageTypeExtensions.isGroupMemberLeave
import ro.aenigma.util.ZonedDateTimeExtensions.normalize
import java.time.ZonedDateTime

object MessageDtoExtensions {
    @JvmStatic
    fun MessageDto.toEntity(): MessageEntity {
        return MessageEntity(
            id = id,
            chatId = chatId,
            senderAddress = senderAddress,
            serverUUID = serverUUID,
            text = text,
            type = type,
            actionFor = actionFor,
            refId = refId,
            incoming = incoming,
            sent = sent,
            deleted = deleted,
            date = date,
            dateReceivedOnServer = dateReceivedOnServer,
            files = files
        )
    }

    @JvmStatic
    fun MessageDto.getMessageTextByAction(context: Context): String? {
        return when (type) {
            MessageType.HELLO -> context.getString(R.string.new_connection_created)
            MessageType.DELETE -> context.getString(R.string.message_deleted)
            MessageType.DELETE_ALL -> context.getString(R.string.conversation_deleted)
            MessageType.GROUP_CREATE -> context.getString(R.string.created_channel)
            MessageType.GROUP_MEMBER_ADD -> context.getString(R.string.added_channel_members)
            MessageType.GROUP_MEMBER_REMOVE -> context.getString(R.string.removed_channel_members)
            MessageType.GROUP_MEMBER_LEAVE -> context.getString(R.string.channel_member_left)
            MessageType.GROUP_RENAMED -> context.getString(R.string.channel_renamed)
            MessageType.FILES -> context.getString(R.string.files)
            MessageType.TEXT, MessageType.REPLY, null -> if (text.isNullOrBlank()) {
                null
            } else {
                text
            }
        }
    }

    @JvmStatic
    fun MessageDto.attachmentsNotAvailable(): Boolean {
        return incoming && type == MessageType.FILES && files.isNullOrEmpty() && filesLate.value.isEmpty()
    }

    @JvmStatic
    fun MessageDto.isNotSent(): Boolean {
        return !incoming && !sent
    }

    @JvmStatic
    fun MessageDto.isText(): Boolean {
        return type == MessageType.TEXT || type == MessageType.REPLY
    }

    @JvmStatic
    fun MessageDto.isHello(): Boolean {
        return type == MessageType.HELLO
    }

    @JvmStatic
    fun MessageDto.isFile(): Boolean {
        return type == MessageType.FILES
    }

    @JvmStatic
    fun MessageDto.isGroupCreateOrUpdate(): Boolean {
        return type.isGroupCreateOrUpdate()
    }

    @JvmStatic
    fun MessageDto.isGroupMemberLeave(): Boolean {
        return type.isGroupMemberLeave()
    }

    @JvmStatic
    fun MessageDto.isDeleteAll(): Boolean {
        return type == MessageType.DELETE_ALL
    }

    @JvmStatic
    fun MessageDto.isDelete(): Boolean {
        return type == MessageType.DELETE
    }

    @JvmStatic
    fun MessageDto.isDeleteOrDeleteAll(): Boolean {
        return isDeleteAll() || isDelete()
    }

    @JvmStatic
    fun MessageDto.withSenderAddress(senderAddress: String?): MessageDto {
        return copy(senderAddress = senderAddress)
    }

    @JvmStatic
    fun MessageDto.markAsSent(): MessageDto {
        return copy(sent = true)
    }

    @JvmStatic
    fun MessageDto.markAsDeleted(): MessageDto {
        return copy(deleted = true)
    }

    @JvmStatic
    fun MessageDto.toArtifactDto(
        senderName: String?,
        guardAddress: String?,
        guardHostname: String?,
        resourceUrl: String?,
        chatId: String?,
        passphrase: String?
    ): ArtifactDto {
        return ArtifactDto(
            text = text,
            type = type,
            actionFor = actionFor,
            senderName = senderName,
            guardAddress = guardAddress,
            guardHostname = guardHostname,
            refId = refId,
            resourceUrl = resourceUrl,
            senderAddress = senderAddress,
            chatId = chatId,
            passphrase = passphrase
        )
    }

    @JvmStatic
    fun MessageDto.getDateTime(): ZonedDateTime? {
        return (dateReceivedOnServer ?: date).normalize()
    }
}
