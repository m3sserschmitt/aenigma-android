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

package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE

object MessageEntityExtensions {
    @JvmStatic
    fun List<MessageWithDetailsDto>.isFullPage(): Boolean {
        return size == CONVERSATION_PAGE_SIZE
    }

    @JvmStatic
    fun MessageEntity.toDto(): MessageDto {
        return MessageDto(
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
}
