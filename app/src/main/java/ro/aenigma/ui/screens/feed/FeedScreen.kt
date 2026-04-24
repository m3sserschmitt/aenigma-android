package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.isFullyExpanded
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.isNotFullyExpanded
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toExpanded
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toPartiallyExpanded
import ro.aenigma.models.factories.NewPostSheetStateDtoFactory
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.ui.screens.common.ComposeNewArticleAppBarAction
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.ReloadAppBarAction
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.BottomSheetScaffoldStateExtensions.isNotFullyExpanded
import ro.aenigma.util.Constants.Companion.BOTTOM_SHEET_PEEK_HEIGHT
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun FeedScreen(
    mainViewModel: MainViewModel,
    navigateToArticle: (uri: String, title: String?, messageId: Long?) -> Unit,
    redirectUri: (String) -> Unit
) {
    val articles by mainViewModel.newsFeed.collectAsState()
    val newPostSheetState by mainViewModel.newPostSheetState.collectAsState()

    FeedScreen(
        articles = articles,
        newPostSheetState = newPostSheetState,
        okHttpClientProvider = mainViewModel.provideOkHttpClientProvider(),
        onArticleClicked = { article ->
            if (!article.url.isNullOrBlank()) {
                navigateToArticle(article.url, article.title, article.messageId)
            }
        },
        onNewPostSheetStateChanged = { sheetState -> mainViewModel.setNewPostSheetState(sheetState) },
        onReloadFeedClicked = { mainViewModel.reloadFeed() },
        onPostClicked = { mainViewModel.postArticle() },
        onRedirectUriClicked = redirectUri
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    articles: RequestState<List<ArticleDto>>,
    newPostSheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    okHttpClientProvider: IOkHttpClientProvider,
    onArticleClicked: (ArticleDto) -> Unit,
    onNewPostSheetStateChanged: (NewPostSheetStateDto) -> Unit = { },
    onReloadFeedClicked: () -> Unit = { },
    onPostClicked: () -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { }
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = newPostSheetState.sheetState
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    LaunchedEffect(key1 = newPostSheetState.isNotFullyExpanded()) {
        if (newPostSheetState.isNotFullyExpanded()) {
            bottomSheetState.partialExpand()
        } else {
            bottomSheetState.expand()
        }
    }

    LaunchedEffect(key1 = bottomSheetScaffoldState.isNotFullyExpanded()) {
        if (newPostSheetState.isFullyExpanded() && bottomSheetScaffoldState.isNotFullyExpanded()) {
            onNewPostSheetStateChanged(newPostSheetState.toPartiallyExpanded())
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = BOTTOM_SHEET_PEEK_HEIGHT,
        sheetContainerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StandardAppBar(
                title = stringResource(id = R.string.news),
                navigateBackVisible = false,
                actions = {
                    ReloadAppBarAction(
                        visible = true,
                        tint = MaterialTheme.colorScheme.onBackground,
                        onClick = onReloadFeedClicked
                    )
                },
                navigateBackAlternative = {
                    ComposeNewArticleAppBarAction(
                        tint = MaterialTheme.colorScheme.onBackground,
                        onComposeNewArticle = {
                            if (bottomSheetScaffoldState.isNotFullyExpanded()) {
                                onNewPostSheetStateChanged(newPostSheetState.toExpanded())
                            } else {
                                onNewPostSheetStateChanged(newPostSheetState.toPartiallyExpanded())
                            }
                        }
                    )
                }
            )
        },
        sheetContent = {
            NewPostBottomSheet(
                sheetState = newPostSheetState,
                onSheetStateChanged = onNewPostSheetStateChanged,
                onPostClicked = onPostClicked
            )
        }
    ) { padding ->
        FeedScreenContent(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = BOTTOM_SHEET_PEEK_HEIGHT
            ).fillMaxSize(),
            articles = articles,
            okHttpClientProvider = okHttpClientProvider,
            onArticleClicked = onArticleClicked,
            onRedirectUriClicked = onRedirectUriClicked
        )
    }
}

@Composable
fun FeedScreenContent(
    modifier: Modifier,
    articles: RequestState<List<ArticleDto>>,
    okHttpClientProvider: IOkHttpClientProvider,
    onArticleClicked: (ArticleDto) -> Unit,
    onRedirectUriClicked: (String) -> Unit = { }
) {
    when (articles) {
        is RequestState.Success -> {
            if (articles.data.isNotEmpty()) {
                FeedList(
                    modifier = modifier,
                    articles = articles.data,
                    okHttpClientProvider = okHttpClientProvider,
                    onArticleClicked = onArticleClicked,
                    onRedirectUriClicked = onRedirectUriClicked
                )
            } else {
                EmptyFeedScreen(modifier = modifier)
            }
        }

        is RequestState.Error -> ErrorScreen(
            modifier = modifier,
            text = stringResource(id = R.string.something_went_wrong)
        )

        RequestState.Idle,
        RequestState.Loading -> LoadingScreen(modifier = modifier)
    }
}

@Preview
@Composable
fun FeedScreenPreview() {
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
    FeedScreen(
        articles = RequestState.Success(articles),
        okHttpClientProvider = OkHttpClientProviderDefault(),
        onArticleClicked = {},
        onReloadFeedClicked = {}
    )
}
