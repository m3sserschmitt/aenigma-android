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
import ro.aenigma.util.StringExtensions.isRemoteUri
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun ArticleScreen(
    uri: String?,
    title: String? = null,
    messageId: Long? = null,
    mainViewModel: MainViewModel,
    forwardMessage: (Long) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(key1 = uri) { mainViewModel.fetchArticle(uri) }

    val content by mainViewModel.articleContent.collectAsState()
    val context = LocalContext.current

    ArticleScreen(
        content = content,
        title = title,
        imageTransformer = mainViewModel.provideMarkdownImageTransformer(),
        onShareArticle = {
            if (uri.isRemoteUri()) {
                context.shareText(String.format(WEB_ARTICLE_URL_TEMPLATE, Uri.encode(uri)))
            } else if(messageId != null){
                forwardMessage(messageId)
            }
        },
        navigateBack = navigateBack
    )
}

@Composable
fun ArticleScreen(
    content: RequestState<String>,
    title: String? = null,
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
    onShareArticle: () -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StandardAppBar(
                title = title.takeIf { t -> !t.isNullOrBlank() } ?: "",
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
