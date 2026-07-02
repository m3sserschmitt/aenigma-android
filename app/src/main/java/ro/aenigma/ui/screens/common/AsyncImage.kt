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

package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import ro.aenigma.R
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.util.StringExtensions.isRemoteUri
import ro.aenigma.util.rememberImageLoader

@Composable
private fun RemoteAsyncImage(
    modifier: Modifier = Modifier,
    uri: String,
    okHttpClientProvider: IOkHttpClientProvider,
    contentScale: ContentScale = ContentScale.Fit
) {
    val imageLoader = rememberImageLoader(okHttpClientProvider, uri)
    if(imageLoader != null) {
        val context = LocalContext.current
        AsyncImage(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .build(),
            contentDescription = stringResource(id = R.string.picture),
            contentScale = contentScale,
            imageLoader = imageLoader
        )
    }
}

@Composable
fun AsyncImage(
    modifier: Modifier = Modifier,
    uri: String,
    okHttpClientProvider: IOkHttpClientProvider,
    contentScale: ContentScale = ContentScale.Fit
) {
    val isRemote = remember(key1 = uri) { uri.isRemoteUri() }
    if (isRemote) {
        RemoteAsyncImage(
            uri = uri,
            okHttpClientProvider = okHttpClientProvider,
            contentScale = contentScale
        )
    } else {
        val context = LocalContext.current
        AsyncImage(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .build(),
            contentDescription = stringResource(id = R.string.picture),
            contentScale = ContentScale.Fit
        )
    }
}
