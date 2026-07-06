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

package ro.aenigma.services

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import dagger.hilt.android.scopes.ViewModelScoped
import ro.aenigma.R
import ro.aenigma.util.rememberImageLoader
import javax.inject.Inject

@ViewModelScoped
class MarkdownImageTransformer @Inject constructor(
    private val okHttpClientProvider: OkHttpClientProvider
): ImageTransformer {

    @Composable
    override fun transform(link: String): ImageData {
        val imageLoader = rememberImageLoader(okHttpClientProvider, link)
        val painter = if (imageLoader == null) {
            painterResource(R.drawable.ic_broken_image)
        } else {
            val context = LocalContext.current
            rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(link)
                    .crossfade(true)
                    .error(R.drawable.ic_broken_image)
                    .build(),
                imageLoader = imageLoader
            )
        }

        return ImageData(
            painter = painter,
            contentDescription = null,
            alignment = Alignment.CenterStart,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    override fun intrinsicSize(painter: Painter): Size =
        painter.intrinsicSize
}
