package ro.aenigma.ui.screens.feed

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.NoOpImageTransformerImpl
import com.mikepenz.markdown.model.rememberMarkdownState
import ro.aenigma.R
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.ShareTopAppBarAction
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.Constants.Companion.WEB_ARTICLE_URL_TEMPLATE
import ro.aenigma.util.ContextExtensions.shareText
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun ArticleScreen(
    url: String?,
    mainViewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    LaunchedEffect(key1 = url) {
        if (!url.isNullOrBlank()) {
            mainViewModel.fetchArticle(url)
        }
    }

    val articleContent by mainViewModel.articleContent.collectAsState()
    val context = LocalContext.current

    ArticleScreen(
        content = articleContent,
        imageTransformer = mainViewModel.markdownImageTransformer,
        onShareArticle = {
            if (!url.isNullOrBlank()) {
                context.shareText(
                    String.format(
                        WEB_ARTICLE_URL_TEMPLATE,
                        Uri.encode(url).toString()
                    )
                )
            }
        },
        navigateBack = navigateBack
    )
}

@Composable
fun ArticleScreen(
    content: RequestState<String>,
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
    onShareArticle: () -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StandardAppBar(
                title = "",
                navigateBack = navigateBack,
                actions = {
                    ShareTopAppBarAction(
                        visible = true,
                        onClick = onShareArticle,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        },
        content = { paddingValues ->
            ArticleScreenContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 8.dp,
                    end = 8.dp
                ),
                content = content,
                imageTransformer = imageTransformer
            )
        }
    )
}

@Composable
fun ArticleScreenContent(
    modifier: Modifier = Modifier,
    content: RequestState<String>,
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
) {
    when(content) {
        is RequestState.Success -> {
            MarkdownContent(
                modifier = modifier,
                content = content.data,
                imageTransformer = imageTransformer
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
    content: String,
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl()
) {
    val state = rememberMarkdownState(content)
    val scrollState = rememberScrollState()
    Markdown(
        modifier = modifier.verticalScroll(scrollState)
            .background(color = MaterialTheme.colorScheme.background),
        colors = markdownColor(
            text = MaterialTheme.colorScheme.onBackground
        ),
        markdownState = state,
        imageTransformer = imageTransformer
    )
}
