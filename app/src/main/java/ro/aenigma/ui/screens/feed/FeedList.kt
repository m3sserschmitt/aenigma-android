package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ro.aenigma.R
import ro.aenigma.models.Article

@Composable
fun ArticleList(
    modifier: Modifier = Modifier,
    articles: List<Article>,
    onArticleClick: (Article) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = articles,
            key = { it.hashCode() }
        ) { article ->
            ArticleCard(
                article = article,
                modifier = Modifier.clickable { onArticleClick(article) }
            )
        }
    }
}

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    article: Article
) {
    val context = LocalContext.current
    Card(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = article.title ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(
                    modifier = Modifier.height(4.dp)
                )
                Text(
                    text = article.description ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                for(uri in article.imageUrls ?: listOf()) {
                    AsyncImage(modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 12.dp),
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(id = R.string.files),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleListPreview() {
    val articles = List(1) {
        Article(
            title = "Article $it",
            description = "A short description for item $it",
            url = "https://picsum.photos/seed/$it/300/300",
            date = "Aug 25th 2025",
            imageUrls = null
        )
    }
    ArticleList(
        articles = articles,
        onArticleClick = {}
    )
}
