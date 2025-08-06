package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import ro.aenigma.R
import ro.aenigma.models.Article
import ro.aenigma.services.ImageFetcher
import ro.aenigma.services.NoOpImageFetcherImpl
import ro.aenigma.util.isRemoteUri

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
            key = { it.hashCode() }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            Column(Modifier.weight(1f)) {
                if(!article.title.isNullOrBlank())
                {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                }
                if(!article.imageUrls.isNullOrEmpty())
                {
                    for(uri in article.imageUrls) {
                        SecureAsyncImage(
                            uri = uri,
                            imageFetcher = imageFetcher
                        )
                    }
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                }
                if(!article.description.isNullOrBlank()) {
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SecureAsyncImage(
    uri: String?,
    imageFetcher: ImageFetcher
) {
    val isRemote = remember(key1 = uri) { uri.isRemoteUri() }
    val bitmap by produceState<ImageBitmap?>(null, uri) {
        value = try {
            if (isRemote && uri != null) {
                imageFetcher.fetch(uri)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
    val context = LocalContext.current
    if(isRemote && bitmap != null) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .padding(bottom = 12.dp),
            bitmap = bitmap!!,
            contentDescription = stringResource(id = R.string.files),
            contentScale = ContentScale.Fit
        )
    } else {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .padding(bottom = 12.dp),
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.files),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedListPreview() {
    val articles = List(1) {
        Article(
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
