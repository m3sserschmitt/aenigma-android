package ro.aenigma.ui.screens.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.BuildConfig
import ro.aenigma.R

import ro.aenigma.ui.screens.common.StandardAppBar

@Composable
fun AboutScreen(
    navigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        topBar = {
            StandardAppBar(
                title = stringResource(id = R.string.about_app),
                navigateBack = navigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    start = 8.dp,
                    end = 8.dp
                )
                .verticalScroll(scrollState)
        ) {
            Text(
                text = stringResource(
                    id = R.string.app_name_and_version
                ).format(BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.copyright_notices),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.gpl_license),
                style = MaterialTheme.typography.bodyMedium,
            )

            Link(
                context = context,
                url = stringResource(R.string.gnu_license_link)
            )

            Text(
                text = stringResource(R.string.openssl_notice),
                style = MaterialTheme.typography.bodyMedium
            )

            Link(
                context = context,
                url = stringResource(R.string.openssl_link)
            )

            Text(
                text = stringResource(R.string.eric_young_notice),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.source_code_available_at),
                style = MaterialTheme.typography.bodyMedium,
            )

            Link(
                context = context,
                url = stringResource(R.string.source_code_link)
            )

            Text(
                text = stringResource(R.string.disclaimer),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun Link(
    context: Context,
    url: String
) {
    TextButton(
        modifier = Modifier.padding(0.dp),
        onClick = {
            openLinkInBrowser(
                context = context,
                url = url
            )
        },
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = url,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun openLinkInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Preview
@Composable
fun AboutScreenPreview()
{
    AboutScreen(
        navigateBack = { }
    )
}
