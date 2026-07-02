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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import okhttp3.OkHttpClient
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.util.ContextExtensions.createImageLoader

@Composable
fun rememberImageLoader(clientProvider: IOkHttpClientProvider, link: String): ImageLoader? {
    val context = LocalContext.current
    val client by produceState<OkHttpClient?>(
        initialValue = null,
        key1 = clientProvider,
        key2 = link
    ) {
        value = clientProvider.getInstance()
    }
    return remember(client, context) {
        client?.let { client -> context.createImageLoader(client) }
    }
}
