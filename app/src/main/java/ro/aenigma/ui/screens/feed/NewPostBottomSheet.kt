package ro.aenigma.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.NoOpImageTransformerImpl
import com.mikepenz.markdown.model.rememberMarkdownState
import ro.aenigma.R
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.enums.NewPostSheetSection
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.isContentPreviewSection
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.isEditSection
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.isCoverPreviewSection
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toArticleDto
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toContentPreviewSection
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toEditSection
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toCoverPreviewSection
import ro.aenigma.models.factories.NewPostSheetStateDtoFactory
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.ui.screens.common.BottomSheetTemplate
import ro.aenigma.ui.screens.common.BottomSheetTitle
import ro.aenigma.ui.screens.common.FilesCountIndicator
import ro.aenigma.ui.screens.common.FilesSelector
import ro.aenigma.ui.screens.common.PrimaryButton
import ro.aenigma.ui.screens.common.SimpleInfoScreen
import ro.aenigma.ui.screens.common.SimpleOutlineTextInput
import ro.aenigma.util.Constants.Companion.INFO_SCREEN_ICON_SIZE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArticleSection(
    modifier: Modifier = Modifier,
    sheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    onSheetStateChanged: (NewPostSheetStateDto) -> Unit = { },
    onPostClicked: () -> Unit = { }
) {
    var titleError by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf(false) }

    BottomSheetTitle(
        title = stringResource(id = R.string.compose_article)
    )

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        SimpleOutlineTextInput(
            modifier = Modifier.fillMaxWidth(),
            value = sheetState.title,
            label = stringResource(id = R.string.title),
            isError = titleError,
            onValueChanged = { newValue ->
                onSheetStateChanged(sheetState.copy(title = newValue))
                titleError = newValue.isBlank()
            }
        )
        SimpleOutlineTextInput(
            modifier = Modifier.fillMaxWidth(),
            value = sheetState.description,
            label = stringResource(id = R.string.optional_description),
            onValueChanged = { newValue -> onSheetStateChanged(sheetState.copy(description = newValue)) },
            singleLine = false
        )
        FilesCountIndicator(
            modifier = Modifier.fillMaxWidth(),
            count = sheetState.fileUris.size,
            onRemoveAttachments = {
                onSheetStateChanged(sheetState.copy(fileUris = listOf()))
            }
        )
        FilesSelector(
            modifier = Modifier.fillMaxWidth(),
            sheetState = sheetState,
            onSheetStateChanged = onSheetStateChanged
        )
        SimpleOutlineTextInput(
            modifier = Modifier.fillMaxWidth().weight(1f),
            value = sheetState.content,
            label = stringResource(id = R.string.content),
            isError = contentError,
            onValueChanged = { newValue ->
                onSheetStateChanged(sheetState.copy(content = newValue))
                contentError = newValue.isBlank()
            },
            singleLine = false
        )
        PrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.post),
            onClick = {
                contentError = sheetState.content.isBlank()
                titleError = sheetState.title.isBlank()
                if(!contentError && !titleError) {
                    onPostClicked()
                }
            }
        )
    }
}

@Composable
fun EmptyContentSection(
    modifier: Modifier = Modifier
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(id = R.string.empty_article_content)
    ) {
        ContentPreviewSectionIcon(
            modifier = Modifier.size(INFO_SCREEN_ICON_SIZE)
        )
    }
}

@Composable
fun EmptyCoverSection(
    modifier: Modifier = Modifier
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(id = R.string.empty_article_cover)
    ) {
        CoverPreviewSectionIcon(
            modifier = Modifier.size(INFO_SCREEN_ICON_SIZE)
        )
    }
}

@Composable
fun ContentPreviewSection(
    modifier: Modifier = Modifier,
    sheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
) {
    val markdownState = rememberMarkdownState(sheetState.content)

    BottomSheetTitle(
        title = stringResource(id = R.string.content_preview)
    )

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        if(sheetState.content.isBlank())
        {
            EmptyContentSection(
                modifier = Modifier.weight(1f)
            )
        } else {
            Markdown(
                modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.background
                    ).fillMaxSize(),
                colors = markdownColor(
                    text = MaterialTheme.colorScheme.onBackground,
                ),
                markdownState = markdownState,
                imageTransformer = imageTransformer
            )
        }
    }
}

@Composable
fun CoverPreviewSection(
    modifier: Modifier = Modifier,
    sheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    okHttpClientProvider: IOkHttpClientProvider = OkHttpClientProviderDefault()
) {
    BottomSheetTitle(
        title = stringResource(id = R.string.cover_preview)
    )

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        if (sheetState.title.isBlank() && sheetState.description.isBlank() && sheetState.fileUris.isEmpty()) {
            EmptyCoverSection(
                modifier = Modifier.weight(1f)
            )
        } else {
            ArticleCard(
                modifier = Modifier.fillMaxWidth(),
                article = sheetState.toArticleDto(),
                okHttpClientProvider = okHttpClientProvider
            )
        }
    }
}

@Composable
fun SheetContent(
    modifier: Modifier = Modifier,
    sheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
    okHttpClientProvider: IOkHttpClientProvider = OkHttpClientProviderDefault(),
    onSheetStateChanged: (NewPostSheetStateDto) -> Unit = { },
    onPostClicked: () -> Unit = { }
) {
    when (sheetState.selectedSection) {
        NewPostSheetSection.EDIT -> {
            EditArticleSection(
                modifier = modifier,
                sheetState = sheetState,
                onSheetStateChanged = onSheetStateChanged,
                onPostClicked = onPostClicked
            )
        }

        NewPostSheetSection.COVER_PREVIEW -> {
            CoverPreviewSection(
                modifier = modifier,
                sheetState = sheetState,
                okHttpClientProvider = okHttpClientProvider
            )
        }

        NewPostSheetSection.CONTENT_PREVIEW -> {
            ContentPreviewSection(
                modifier = modifier,
                sheetState = sheetState,
                imageTransformer = imageTransformer
            )
        }
    }
}

@Composable
fun EditArticleSectionIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Filled.ModeEdit,
        contentDescription = stringResource(id = R.string.compose_article),
        tint = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun CoverPreviewSectionIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_gallery_thumbnail),
        contentDescription = stringResource(id = R.string.cover_preview),
        tint = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun ContentPreviewSectionIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_markdown),
        contentDescription = stringResource(id = R.string.content_preview),
        tint = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun NewPostBottomSheet(
    sheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
    okHttpClientProvider: IOkHttpClientProvider = OkHttpClientProviderDefault(),
    onSheetStateChanged: (NewPostSheetStateDto) -> Unit = { },
    onPostClicked: () -> Unit = { }
) {
    BottomSheetTemplate(
        navigationBarItems = {
            NavigationBarItem(
                selected = sheetState.isEditSection(),
                onClick = { onSheetStateChanged(sheetState.toEditSection()) },
                icon = {
                    EditArticleSectionIcon()
                }
            )
            NavigationBarItem(
                selected = sheetState.isCoverPreviewSection(),
                onClick = { onSheetStateChanged(sheetState.toCoverPreviewSection()) },
                icon = {
                    CoverPreviewSectionIcon()
                }
            )

            NavigationBarItem(
                selected = sheetState.isContentPreviewSection(),
                onClick = { onSheetStateChanged(sheetState.toContentPreviewSection()) },
                icon = {
                    ContentPreviewSectionIcon()
                }
            )
        }
    ) {
        SheetContent(
            modifier = Modifier.weight(1f),
            sheetState = sheetState,
            imageTransformer = imageTransformer,
            okHttpClientProvider = okHttpClientProvider,
            onSheetStateChanged = onSheetStateChanged,
            onPostClicked = onPostClicked
        )
    }
}
