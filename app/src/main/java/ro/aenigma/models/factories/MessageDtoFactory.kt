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

package ro.aenigma.models.factories

import ro.aenigma.models.MessageDto
import ro.aenigma.models.enums.MessageType
import ro.aenigma.util.Constants.Companion.BROADCAST_CONTACT_ADDRESS
import java.time.ZonedDateTime
import java.util.UUID

object MessageDtoFactory {
    @JvmStatic
    fun createIncoming(
        chatId: String, senderAddress: String?, serverUUID: String?, text: String?,
        type: MessageType?, actionFor: String?, refId: String?,
        dateReceivedOnServer: ZonedDateTime?, attachments: List<String> = listOf()
    ): MessageDto {
        return MessageDto(
            id = 0,
            chatId = chatId,
            senderAddress = senderAddress,
            serverUUID = serverUUID,
            text = text,
            type = type,
            actionFor = actionFor,
            refId = refId,
            incoming = true,
            sent = false,
            deleted = false,
            date = ZonedDateTime.now(),
            dateReceivedOnServer = dateReceivedOnServer,
            files = attachments
        )
    }

    @JvmStatic
    fun createOutgoing(
        chatId: String,
        text: String?,
        type: MessageType?,
        actionFor: String?,
        attachments: List<String> = listOf()
    ): MessageDto {
        return MessageDto(
            id = 0,
            chatId = chatId,
            senderAddress = null,
            serverUUID = null,
            text = text,
            type = type,
            actionFor = actionFor,
            refId = UUID.randomUUID().toString(),
            incoming = false,
            sent = false,
            deleted = false,
            date = ZonedDateTime.now(),
            dateReceivedOnServer = null,
            files = attachments
        )
    }

    @JvmStatic
    fun createOutgoingBroadcast(
        text: String?,
        type: MessageType?,
        actionFor: String?,
        attachments: List<String> = listOf()
    ): MessageDto {
        return createOutgoing(
            chatId = BROADCAST_CONTACT_ADDRESS,
            text = text,
            type = type,
            actionFor = actionFor,
            attachments = attachments
        )
    }

    @JvmStatic
    fun createOutgoingHelloMessage(
        chaId: String
    ): MessageDto {
        return createOutgoing(
            chatId = chaId,
            text = null,
            type = MessageType.HELLO,
            actionFor = null
        )
    }
}
