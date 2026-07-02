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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ro.aenigma.models.ArticleDto
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.util.ContextExtensions.showImageViewer
import ro.aenigma.util.ZonedDateTimeExtensions.messageCardStyleFormat

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    article: ArticleDto,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    onItemSelected: (ArticleDto) -> Unit = { },
    onItemDeselected: (ArticleDto) -> Unit = { },
    okHttpClientProvider: IOkHttpClientProvider,
    onClick: (ArticleDto) -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { }
) {
    val date = remember(key1 = article.date) {
        article.date?.messageCardStyleFormat()
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Card(
        modifier = modifier.selectable(
            item = article,
            isSelectionMode = isSelectionMode,
            isSelected = isSelected,
            onItemSelected = onItemSelected,
            onItemDeselected = onItemDeselected,
            onClick = { item ->
                onClick(item)
                coroutineScope.launch {
                    context.showImageViewer(
                        article = article
                    )
                }
            }
        ).fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionModeBullet(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                contentColor = contentColor
            )
            Column(
                Modifier.background(color = containerColor)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (!article.title.isNullOrBlank()) {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!article.author.isNullOrEmpty()) {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = article.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor
                        )
                        Text(
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                            text = "|",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor
                        )
                    }
                    if (!date.isNullOrEmpty()) {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor
                        )
                    }
                }

                FilesList(
                    uris = article.imageUrls?.mapNotNull { uri -> uri } ?: listOf(),
                    contentColor = contentColor,
                    okHttpClientProvider = okHttpClientProvider,
                    onRedirectUriClicked = onRedirectUriClicked
                )

                if (!article.description.isNullOrBlank()) {
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
        }
    }
}
