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

package ro.aenigma.util

import org.ocpsoft.prettytime.PrettyTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object ZonedDateTimeExtensions {
    @JvmStatic
    fun String.messageCardStyleFormat(): String? {
        return normalize()?.messageCardStyleFormat()
    }

    @JvmStatic
    fun ZonedDateTime.messageCardStyleFormat(): String? {
        return try {
            val now = ZonedDateTime.now()
            val targetDateTime = normalize() ?: return null
            val daysAgo = ChronoUnit.DAYS.between(targetDateTime, now)
            val yearsAgo = ChronoUnit.YEARS.between(targetDateTime, now)
            val differentDays = targetDateTime.toLocalDate() != now.toLocalDate()
            val formatter = when {
                daysAgo < 1 && differentDays -> DateTimeFormatter.ofPattern("EEE, HH:mm")
                daysAgo < 1 -> DateTimeFormatter.ofPattern("HH:mm")
                yearsAgo < 1 -> DateTimeFormatter.ofPattern("EEE, d MMM, HH:mm")
                else -> DateTimeFormatter.ofPattern("EEE, d MMM yyyy, HH:mm")
            }
            targetDateTime.format(formatter)
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun ZonedDateTime.chatroomStyleFormat(): String? {
        return try {
            val now = ZonedDateTime.now()
            val targetDateTime = withZoneSameInstant(ZoneId.systemDefault())
            val daysAgo = ChronoUnit.DAYS.between(targetDateTime, now)
            val yearsAgo = ChronoUnit.YEARS.between(targetDateTime, now)

            if (daysAgo < 1) {
                PrettyTime().formatUnrounded(targetDateTime)
            } else if (daysAgo < 7) {
                val formater = DateTimeFormatter.ofPattern("EEE")
                targetDateTime.format(formater)
            } else if (yearsAgo < 1) {
                val formater = DateTimeFormatter.ofPattern("EEE, d MMM")
                targetDateTime.format(formater)
            } else {
                val formater = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
                targetDateTime.format(formater)
            }
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun String.normalize(): ZonedDateTime? {
        return try {
            ZonedDateTime.parse(this).withZoneSameInstant(ZoneId.systemDefault())
        } catch (_: Exception) {
            return null
        }
    }

    @JvmStatic
    fun ZonedDateTime.normalize(): ZonedDateTime? {
        return try {
            withZoneSameInstant(ZoneId.systemDefault())
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun String.socialMediaStyleFormat(): String? {
        return try {
            normalize()?.socialMediaStyleFormat()
        } catch (_: Exception) {
            return null
        }
    }

    @JvmStatic
    fun ZonedDateTime.socialMediaStyleFormat(): String? {
        return try {
            PrettyTime().format(this)
        } catch (_: Exception) {
            null
        }
    }
}
