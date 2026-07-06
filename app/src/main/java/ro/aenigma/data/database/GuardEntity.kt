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
import ro.aenigma.util.Constants.Companion.GUARDS_TABLE
import java.time.ZonedDateTime

@Entity(
    tableName = GUARDS_TABLE,
    indices = [Index(value = ["address"], unique = true)]
)
data class GuardEntity (
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val address: String,
    val publicKey: String,
    val hostname: String?,
    val onionService: String?,
    val graphVersion: String?,
    val dateCreated: ZonedDateTime
)
