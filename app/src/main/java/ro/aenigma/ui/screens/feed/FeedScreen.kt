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
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun ArticlesScreen(
    mainViewModel: MainViewModel
) {
    val articles by mainViewModel.latestNews.collectAsState()

    LaunchedEffect(key1 = true) {
        mainViewModel.collectFeed()
    }

    ArticlesScreen(
        articles = articles
    )
}

@Composable
fun ArticlesScreen(
    articles: RequestState<List<Article>>
) {
    Scaffold(
        topBar = {
            StandardAppBar(
                title = stringResource(id = R.string.news),
                navigateBackVisible = false
            )
        }
    ) { padding ->
        ArticlesScreenContent(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding()
            ).fillMaxSize(),
            articles = articles
        )
    }
}

@Composable
fun ArticlesScreenContent(
    modifier: Modifier,
    articles: RequestState<List<Article>>
) {
    when(articles) {
        is RequestState.Success -> {
            if(articles.data.isNotEmpty())
            {
                ArticleList(
                    modifier = modifier,
                    articles = articles.data
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
fun ArticlesScreenPreview()
{
    val articles = List(1) {
        Article(
            title = "Article $it",
            description = "A short description for item $it",
            url = "https://picsum.photos/seed/$it/300/300",
            date = "Aug 25th 2025",
            imageUrls = null
        )
    }
    ArticlesScreen(
        articles = RequestState.Success(articles)
    )
}
