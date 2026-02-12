package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.models.ArticleDto
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.ui.screens.common.FilesList
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_METADATA_FILE

@Composable
fun FeedList(
    modifier: Modifier = Modifier,
    articleDtos: List<ArticleDto>,
    okHttpClientProvider: IOkHttpClientProvider,
    onArticleClicked: (ArticleDto) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = articleDtos,
            key = { article -> article.hashCode() }
        ) { article ->
            ArticleCard(
                modifier = Modifier.clickable { onArticleClicked(article) },
                articleDto = article,
                okHttpClientProvider = okHttpClientProvider
            )
        }
    }
}

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    articleDto: ArticleDto,
    okHttpClientProvider: IOkHttpClientProvider
) {
    Card(
        modifier
            .fillMaxWidth()
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
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (!articleDto.title.isNullOrBlank()) {
                Text(
                    text = articleDto.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
            }
            FilesList(
                uris = articleDto.imageUrls?.mapNotNull { uri -> uri }
                    ?.filter { item -> !item.endsWith(ATTACHMENTS_METADATA_FILE) }
                    ?: listOf(),
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                okHttpClientProvider = okHttpClientProvider
            )
            Spacer(
                modifier = Modifier.height(4.dp)
            )
            if (!articleDto.description.isNullOrBlank()) {
                Text(
                    text = articleDto.description,
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
    val articleDtos = List(1) {
        ArticleDto(
            id = 1,
            title = "Article $it",
            description = "A short description for item $it",
            url = "https://picsum.photos/seed/$it/300/300",
            date = "Aug 25th 2025",
            imageUrls = null
        )
    }
    FeedList(
        articleDtos = articleDtos,
        okHttpClientProvider = OkHttpClientProviderDefault(),
        onArticleClicked = { }
    )
}
