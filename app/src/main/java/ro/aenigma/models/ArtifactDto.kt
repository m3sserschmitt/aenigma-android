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

package ro.aenigma.models

import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime

data class ArtifactDto(
    val text: String? = null,
    val type: MessageType? = null,
    val senderName: String? = null,
    val resourceUrl: String? = null,
    val passphrase: String? = null,
    val guardAddress: String? = null,
    val guardHostname: String? = null,
    val senderAddress: String? = null,
    val refId: String? = null,
    val actionFor: String? = null,
    val chatId: String? = null,
    val dateTimeCreated: ZonedDateTime? = ZonedDateTime.now()
)
