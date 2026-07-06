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

import android.net.Uri
import androidx.core.net.toUri
import ro.aenigma.util.Constants.Companion.SHARE_API_PATH

object UriExtensions {
    @JvmStatic
    fun Uri.isRemote(): Boolean {
        return scheme in listOf("http", "https")
    }

    @JvmStatic
    fun Uri.isSharedData(): Boolean {
        return path.equals(SHARE_API_PATH, ignoreCase = true)
    }

    @JvmStatic
    fun Uri.getArticleUri(): Uri? {
        return try {
            val regex = Regex("[?&]url=([^&#]+)")
            val match = regex.find(toString().lowercase())
            val encodedValue = match?.groups?.get(1)?.value
            val decodedValue = encodedValue?.let { Uri.decode(it) }
            decodedValue?.toUri()
        } catch (_: Exception) {
            null
        }
    }
}
