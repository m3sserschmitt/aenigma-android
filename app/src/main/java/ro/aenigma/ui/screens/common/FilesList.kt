package ro.aenigma.ui.screens.common

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import ro.aenigma.R
import ro.aenigma.models.FileDisplayInfoDto
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.factories.NewPostSheetStateDtoFactory
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_MAX_COUNT
import ro.aenigma.util.Constants.Companion.ATTACHMENT_MAX_SIZE
import ro.aenigma.util.ContextExtensions.getFileTypeIcon
import ro.aenigma.util.ContextExtensions.sizeOf
import ro.aenigma.util.StringExtensions.isRemoteUri
import kotlin.collections.forEach

@Composable
fun FilesList(
    uris: List<String>,
    contentColor: Color = Color.Unspecified,
    okHttpClientProvider: IOkHttpClientProvider,
    onRedirectUriClicked: (String) -> Unit = { }
) {
    if(uris.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            uris.forEach { uri ->
                FileItem(
                    uri = uri,
                    contentColor = contentColor,
                    okHttpClientProvider = okHttpClientProvider,
                    onRedirectUriClicked = onRedirectUriClicked
                )
            }
        }
    } else {
        NoFilesWarning(
            color = contentColor
        )
    }
}

@Composable
fun NoFilesWarning(
    color: Color = Color.Unspecified
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .alpha(0.75f)
                .padding(end = 4.dp),
            painter = painterResource(id = R.drawable.ic_attachement),
            contentDescription = stringResource(R.string.no_files_available),
            tint = color
        )
        Text(
            text = stringResource(id = R.string.no_files_available),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = color
        )
    }
}

@Composable
fun rememberFileDisplayInfo(uri: String): State<FileDisplayInfoDto> {
    val context = LocalContext.current
    return produceState(initialValue = FileDisplayInfoDto(
        painterResourceId = null,
        isImage = false
    ), key1 = uri) {
        value = context.getFileTypeIcon(uri)
    }
}

@Composable
fun getUriTitle(uri: String): String {
    val parsedUri = uri.toUri()
    return if (uri.isRemoteUri()) {
        parsedUri.host
    } else {
        parsedUri.lastPathSegment
    } ?: stringResource(id = R.string.unknown_file)
}

@Composable
fun FileItem(
    uri: String,
    contentColor: Color = Color.Unspecified,
    okHttpClientProvider: IOkHttpClientProvider,
    onRedirectUriClicked: (String) -> Unit = { },
) {
    val fileDisplayInfo by rememberFileDisplayInfo(uri)
    if (fileDisplayInfo.isImage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier.weight(1f),
                uri = uri,
                okHttpClientProvider = okHttpClientProvider
            )
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                RedirectUriButton(
                    tint = contentColor,
                    onClick = { onRedirectUriClicked(uri) }
                )
                ShareUriButton(
                    uri = uri,
                    tint = contentColor
                )
                OpenInExternalAppButton(
                    uri = uri,
                    tint = contentColor
                )
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val resourceId = fileDisplayInfo.painterResourceId
                if (resourceId != null) {
                    Icon(
                        modifier = Modifier.alpha(.75f).size(36.dp),
                        painter = painterResource(resourceId),
                        contentDescription = stringResource(R.string.files),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = getUriTitle(uri),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    RedirectUriButton(
                        tint = MaterialTheme.colorScheme.onPrimary,
                        onClick = { onRedirectUriClicked(uri) }
                    )
                    ShareUriButton(
                        uri = uri,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    OpenInExternalAppButton(
                        uri = uri,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    IndeterminateCircularIndicator(
                        visible = true,
                        size = 36.dp,
                        text = stringResource(id = R.string.loading),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun rememberFilesPicker(
    onFilesSelected: (List<String>) -> Unit,
    maxCount: Int = ATTACHMENTS_MAX_COUNT,
    maxSizeBytes: Long = ATTACHMENT_MAX_SIZE
): ManagedActivityResultLauncher<Array<String>, List<@JvmSuppressWildcards Uri>> {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val tooManyAttachmentsString =
        stringResource(id = R.string.attachment_files_limit, ATTACHMENTS_MAX_COUNT)
    val fileTooLargeString =
        stringResource(id = R.string.files_too_large, maxSizeBytes / (1024.0 * 1024.0))
    val coroutineScope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        coroutineScope.launch {
            var fileTooLarge = false
            val filteredUris = uris.filter { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    if (context.sizeOf(uri) > maxSizeBytes) {
                        fileTooLarge = true
                        false
                    } else {
                        true
                    }
                } catch (_: SecurityException) {
                    false
                }
            }
            if (fileTooLarge) {
                Toast.makeText(context, fileTooLargeString, Toast.LENGTH_LONG).show()
            }
            if (filteredUris.size > maxCount) {
                Toast.makeText(context, tooManyAttachmentsString, Toast.LENGTH_LONG).show()
            }
            onFilesSelected(filteredUris.map { it.toString() })
        }
    }
}

@Composable
fun FilesPickerButton(
    modifier: Modifier = Modifier,
    onFilesSelected: (List<String>) -> Unit = { }
) {
    val multiplePhotoPicker = rememberFilesPicker(
        onFilesSelected = onFilesSelected
    )
    IconButton(
        modifier = modifier,
        onClick = {
            multiplePhotoPicker.launch(arrayOf("*/*"))
        }
    ) {
        Icon(
            modifier = Modifier.alpha(.75f),
            painter = painterResource(id = R.drawable.ic_attachement),
            contentDescription = stringResource(
                id = R.string.attach_files
            ),
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesSelector(
    modifier: Modifier = Modifier,
    sheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    onSheetStateChanged: (NewPostSheetStateDto) -> Unit = { }
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.attach_files),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        FilesPickerButton(
            onFilesSelected = { fileUris ->
                onSheetStateChanged(sheetState.copy(fileUris = fileUris))
            }
        )
    }
}

@Composable
fun FilesCountIndicator(
    modifier: Modifier = Modifier,
    count: Int,
    onRemoveAttachments: () -> Unit
) {
    if(count > 0)
    {
        Row(
            modifier = modifier
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .align(alignment = Alignment.CenterVertically),
                text = if (count > 1)
                    stringResource(id = R.string.n_attachments_selected).format(count)
                else
                    stringResource(id = R.string.one_attachment_selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(

                onClick = onRemoveAttachments
            ) {
                Icon(
                    modifier = Modifier.alpha(.75f),
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(
                        id = R.string.close
                    ),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
