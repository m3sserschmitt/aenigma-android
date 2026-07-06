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

package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ro.aenigma.models.enums.MessageType
import ro.aenigma.util.Constants
import java.time.ZonedDateTime

@Entity(
    tableName = Constants.MESSAGES_TABLE,
    indices = [
        Index(value = ["chatId", "deleted", "id"]),
        Index(value = ["refId"], unique = true),
        Index(value = ["serverUUID"], unique = true)
    ]
)
data class MessageEntity (
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
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
    val files: List<String>?
)
