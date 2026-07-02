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

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.MutableStateFlow
import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime

data class MessageDto(
    val id: Long,
    val chatId: String,
    val senderAddress: String?,
    val serverUUID: String?,
    val text: String?,
    val type: MessageType?,
    val actionFor: String?,
    val refId: String?,
    val incoming: Boolean,
    val sent: Boolean,
    val deleted: Boolean,
    val date: ZonedDateTime,
    val dateReceivedOnServer: ZonedDateTime?,
    val files: List<String>?,
    val deliveryStatus: MutableStateFlow<WorkInfo.State?> = MutableStateFlow(null),
    val attachmentDownloadStatus: MutableStateFlow<WorkInfo.State?> = MutableStateFlow(null),
    val filesLate: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageDto) return false
        return id == other.id
    }
}
