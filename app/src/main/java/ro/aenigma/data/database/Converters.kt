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

import androidx.room.TypeConverter
import ro.aenigma.models.GroupDataDto
import ro.aenigma.util.SerializerExtensions.toJson
import ro.aenigma.util.StringExtensions.fromJson
import java.time.ZonedDateTime

class Converters {
    @TypeConverter
    fun zonedDateTimeToString(date: ZonedDateTime?): String {
        return date.toString()
    }

    @TypeConverter
    fun stringToZonedDateTime(date: String?): ZonedDateTime {
        return try {
            ZonedDateTime.parse(date)
        } catch (_: Exception) {
            ZonedDateTime.now()
        }
    }

    @TypeConverter
    fun fromGroupData(value: GroupDataDto?): String {
        return value.toJson() ?: ""
    }

    @TypeConverter
    fun toGroupData(value: String?): GroupDataDto {
        return value.fromJson() ?: GroupDataDto()
    }

    @TypeConverter
    fun toListOfStrings(value: String?): List<String> {
        return value.fromJson() ?: listOf()
    }

    @TypeConverter
    fun fromListOfStrings(value: List<String>?): String {
        return value.toJson() ?: ""
    }
}
