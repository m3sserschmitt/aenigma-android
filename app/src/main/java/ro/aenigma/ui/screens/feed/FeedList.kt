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
