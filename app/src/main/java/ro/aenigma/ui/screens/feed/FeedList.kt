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
import ro.aenigma.models.Article
import ro.aenigma.services.ImageFetcher
import ro.aenigma.services.NoOpImageFetcherImpl
import ro.aenigma.ui.screens.common.FilesList
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_METADATA_FILE

@Composable
fun FeedList(
    modifier: Modifier = Modifier,
    articles: List<Article>,
    imageFetcher: ImageFetcher = NoOpImageFetcherImpl(),
    onArticleClicked: (Article) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = articles,
            key = { article -> article.hashCode() }
        ) { article ->
            ArticleCard(
                modifier = Modifier.clickable { onArticleClicked(article) },
                article = article,
                imageFetcher = imageFetcher
            )
        }
    }
}

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    article: Article,
    imageFetcher: ImageFetcher = NoOpImageFetcherImpl()
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
            if (!article.title.isNullOrBlank()) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
            }
            FilesList(
                uris = article.imageUrls?.mapNotNull { uri -> uri }
                    ?.filter { item -> !item.endsWith(ATTACHMENTS_METADATA_FILE) }
                    ?: listOf(),
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                imageFetcher = imageFetcher
            )
            Spacer(
                modifier = Modifier.height(4.dp)
            )
            if (!article.description.isNullOrBlank()) {
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
        Article(
            id = 1,
            title = "Article $it",
            description = "A short description for item $it",
            url = "https://picsum.photos/seed/$it/300/300",
            date = "Aug 25th 2025",
            imageUrls = null
        )
    }
    FeedList(
        articles = articles,
        onArticleClicked = { }
    )
}
