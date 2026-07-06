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

package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.models.ArticleDto
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.ui.screens.common.ArticleCard
import ro.aenigma.ui.screens.common.ItemsList

@Composable
fun FeedList(
    modifier: Modifier = Modifier,
    articles: List<ArticleDto>,
    okHttpClientProvider: IOkHttpClientProvider,
    listState: LazyListState = rememberLazyListState(),
    onArticleClicked: (ArticleDto) -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { }
) {
    ItemsList(
        modifier = modifier,
        listState = listState,
        items = articles,
        itemKeySelector = { article -> article.hashCode() },
        listItem = { _, item, _ ->
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                ArticleCard(
                    article = item,
                    okHttpClientProvider = okHttpClientProvider,
                    onClick = onArticleClicked,
                    onRedirectUriClicked = onRedirectUriClicked
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FeedListPreview() {
    val articles = List(1) {
        ArticleDto(
            messageId = 1,
            title = "Article $it",
            description = "A short description for item $it",
            url = "https://picsum.photos/seed/$it/300/300",
            date = "Aug 25th 2025",
            imageUrls = null
        )
    }
    FeedList(
        articles = articles,
        okHttpClientProvider = OkHttpClientProviderDefault(),
        onArticleClicked = { }
    )
}
