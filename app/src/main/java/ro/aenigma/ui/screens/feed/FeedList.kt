package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ro.aenigma.models.ArticleDto
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.ui.screens.common.FilesList
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.util.ContextExtensions.showImageViewer

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
            ArticleCard(
                article = item,
                okHttpClientProvider = okHttpClientProvider,
                onClick = { onArticleClicked(item) },
                onRedirectUriClicked = onRedirectUriClicked
            )
        }
    )
}

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    article: ArticleDto,
    okHttpClientProvider: IOkHttpClientProvider,
    onClick: () -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { }
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Card(
        modifier = modifier.clickable {
            onClick()
            coroutineScope.launch {
                context.showImageViewer(
                    article = article
                )
            }
        }.fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (!article.title.isNullOrBlank()) {
                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
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
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                        text = "|",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (!article.date.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = article.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            FilesList(
                uris = article.imageUrls?.mapNotNull { uri -> uri } ?: listOf(),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
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
