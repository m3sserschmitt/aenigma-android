package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.models.Article
import ro.aenigma.services.ImageFetcher
import ro.aenigma.services.NoOpImageFetcherImpl
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.ReloadAppBarAction
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun FeedScreen(
    mainViewModel: MainViewModel,
    navigateToArticle: (String) -> Unit
) {
    val articles by mainViewModel.newsFeed.collectAsState()

    LaunchedEffect(key1 = articles) {
        if(articles is RequestState.Idle)
        {
            mainViewModel.collectFeed()
        }
    }

    FeedScreen(
        articles = articles,
        imageFetcher = mainViewModel.imageFetcher,
        onArticleClicked = { article ->
            if (article.url?.isNotBlank() == true) {
                navigateToArticle(article.url)
            }
        },
        onReloadFeedClicked = { mainViewModel.reloadFeed() }
    )
}

@Composable
fun FeedScreen(
    articles: RequestState<List<Article>>,
    imageFetcher: ImageFetcher = NoOpImageFetcherImpl(),
    onArticleClicked: (Article) -> Unit,
    onReloadFeedClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            StandardAppBar(
                title = stringResource(id = R.string.news),
                navigateBackVisible = false,
                actions = {
                    ReloadAppBarAction(
                        visible = true,
                        onClick = onReloadFeedClicked
                    )
                }
            )
        }
    ) { padding ->
        FeedScreenContent(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding()
            ).fillMaxSize(),
            articles = articles,
            imageFetcher = imageFetcher,
            onArticleClicked = onArticleClicked
        )
    }
}

@Composable
fun FeedScreenContent(
    modifier: Modifier,
    articles: RequestState<List<Article>>,
    imageFetcher: ImageFetcher = NoOpImageFetcherImpl(),
    onArticleClicked: (Article) -> Unit
) {
    when (articles) {
        is RequestState.Success -> {
            if (articles.data.isNotEmpty()) {
                FeedList(
                    modifier = modifier,
                    articles = articles.data,
                    imageFetcher = imageFetcher,
                    onArticleClicked = onArticleClicked
                )
            } else {
                EmptyFeedScreen()
            }
        }

        is RequestState.Error -> ErrorScreen(
            text = stringResource(id = R.string.something_went_wrong)
        )

        RequestState.Idle,
        RequestState.Loading -> LoadingScreen()
    }
}

@Preview
@Composable
fun FeedScreenPreview() {
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
    FeedScreen(
        articles = RequestState.Success(articles),
        onArticleClicked = {},
        onReloadFeedClicked = {}
    )
}
