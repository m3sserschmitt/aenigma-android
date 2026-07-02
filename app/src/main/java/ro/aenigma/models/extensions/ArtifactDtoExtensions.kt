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

import ro.aenigma.models.ArtifactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.extensions.MessageTypeExtensions.isGroupCreate
import ro.aenigma.models.extensions.MessageTypeExtensions.isGroupUpdate
import ro.aenigma.models.factories.MessageDtoFactory
import java.time.ZonedDateTime

object ArtifactDtoExtensions {
    @JvmStatic
    fun ArtifactDto.toMessageDto(
        serverUuid: String,
        dateReceivedOnServer: ZonedDateTime?
    ): MessageDto? {
        return MessageDtoFactory.createIncoming(
            chatId = chatId ?: return null,
            senderAddress = senderAddress ?: return null,
            serverUUID = serverUuid,
            text = text,
            type = type ?: return null,
            actionFor = actionFor,
            refId = refId ?: return null,
            dateReceivedOnServer = dateReceivedOnServer,
        )
    }

    @JvmStatic
    fun ArtifactDto.isGroupUpdate(): Boolean {
        return type.isGroupUpdate()
    }

    @JvmStatic
    fun ArtifactDto.isGroupCreate(): Boolean {
        return type.isGroupCreate()
    }
}
