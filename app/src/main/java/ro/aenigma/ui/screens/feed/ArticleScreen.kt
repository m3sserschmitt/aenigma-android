package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import ro.aenigma.R
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun ArticleScreen(
    url: String,
    mainViewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    LaunchedEffect(key1 = url) {
        mainViewModel.fetchArticle(url)
    }

    val articleContent by mainViewModel.articleContent.collectAsState()

    ArticleScreen(
        content = articleContent,
        navigateBack = navigateBack
    )
}

@Composable
fun ArticleScreen(
    content: RequestState<String>,
    navigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            StandardAppBar(
                title = "",
                navigateBack = navigateBack
            )
        },
        content = { paddingValues ->
            ArticleScreenContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 8.dp,
                    end = 8.dp
                ),
                content = content
            )
        }
    )
}

@Composable
fun ArticleScreenContent(
    modifier: Modifier = Modifier,
    content: RequestState<String>
) {
    when(content) {
        is RequestState.Success -> {
            MarkdownContent(
                modifier = modifier,
                content = content.data
            )
        }
        is RequestState.Idle,
        is RequestState.Loading -> LoadingScreen(
            modifier = modifier
        )
        is RequestState.Error -> ErrorScreen(
            modifier = modifier,
            text = stringResource(
                id = R.string.something_went_wrong
            )
        )
    }
}

@Composable
fun MarkdownContent(
    modifier: Modifier = Modifier,
    content: String
) {
    val state = rememberMarkdownState(content)
    val scrollState = rememberScrollState()
    Markdown(
        markdownState = state,
        modifier = modifier.verticalScroll(scrollState)
    )
}
