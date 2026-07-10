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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.enums.NewPostSheetSection
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
import ro.aenigma.ui.themes.ApplicationComposeDarkTheme
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
        feedListState = mainViewModel.feedListState,
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
    articles: RequestState<List<ArticleDto>> = RequestState.Success(listOf()),
    newPostSheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    okHttpClientProvider: IOkHttpClientProvider = OkHttpClientProviderDefault(),
    feedListState: LazyListState = rememberLazyListState(),
    onArticleClicked: (ArticleDto) -> Unit = { },
    onNewPostSheetStateChanged: (NewPostSheetStateDto) -> Unit = { },
    onReloadFeedClicked: () -> Unit = { },
    onPostClicked: () -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { }
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = newPostSheetState.sheetState,
        skipHiddenState = false
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
        containerColor = MaterialTheme.colorScheme.background,
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
                bottom = BOTTOM_SHEET_PEEK_HEIGHT,
                start = 8.dp,
                end = 8.dp
            ).fillMaxSize(),
            feedListState = feedListState,
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
    feedListState: LazyListState = rememberLazyListState(),
    onArticleClicked: (ArticleDto) -> Unit,
    onRedirectUriClicked: (String) -> Unit = { }
) {
    when (articles) {
        is RequestState.Success -> {
            if (articles.data.isNotEmpty()) {
                FeedList(
                    modifier = modifier,
                    listState = feedListState,
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

private val articlesPreview = RequestState.Success(
    listOf(
        ArticleDto(
            messageId = 1,
            title = "Nothing More",
            author = "Michaela",
            description = "The message arrived just after midnight — three words, nothing more. " +
                    "She read them twice before setting the phone down. Some conversations don't" +
                    " need length to matter. Outside, the city kept its usual rhythm, unaware that " +
                    "somewhere in the dark, a single reply had already changed everything.",
            url = null,
            date = "2026-07-10T17:23:00Z",
            imageUrls = listOf("https://my.blog.com")
        ),
        ArticleDto(
            messageId = 2,
            title = "The Office Coffee Machine Knows",
            author = "Rania",
            description = "Okay but why does the coffee machine at work have more personality than" +
                    " half my coworkers. It hisses when it's annoyed. It takes forever when you're " +
                    "late. And somehow it still makes a better cup than the fancy one I bought for " +
                    "home. Some things just aren't meant to be replaced.",
            url = null,
            date = "2026-07-04T20:34:01Z",
            imageUrls = listOf("https://articles.aenigma.ro/seed-images/coffe-machine.jpg")
        ),
        ArticleDto(
            messageId = 3,
            title = "Cereal Counts as Cooking",
            author = "John",
            description = "So I tried to cook dinner without a recipe tonight. Bold move, I know. " +
                    "Twenty minutes in, the kitchen smelled amazing and I felt like a genius. " +
                    "Ten minutes after that, smoke alarm. Ten minutes after THAT, cereal for dinner. " +
                    "Still counts as cooking, right?",
            url = null,
            date = "2026-06-26T00:00:00Z",
            imageUrls = listOf("https://articles.aenigma.ro/seed-images/cereals.jpg")
        ),
    )
)

@Preview
@Composable
fun FeedScreenPreview() {
    FeedScreen(
        articles = articlesPreview
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FeedScreenStoryBottomSheetPreview() {
    FeedScreen(
        articles = articlesPreview,
        newPostSheetState = NewPostSheetStateDto(
            SheetValue.Expanded,
            selectedSection = NewPostSheetSection.EDIT,
            title = "The To-Do List That Never Opened",
            fileUris = listOf(),
            description = "",
            content = "Tried to be productive today. Made a to-do list, color-coded it, even added " +
                    "little checkboxes. Then spent forty-five minutes deciding which pen to use. " +
                    "Never actually opened the list again. But hey, it looked great sitting on my " +
                    "desk. Productivity is a mindset, right? Right."
        )
    )
}

@Preview
@Composable
fun FeedScreenDarkPreview() {
    ApplicationComposeDarkTheme {
        FeedScreenPreview()
    }
}

@Preview
@Composable
fun FeedScreenStoryBottomSheetDarkPreview() {
    ApplicationComposeDarkTheme {
        FeedScreenStoryBottomSheetPreview()
    }
}
