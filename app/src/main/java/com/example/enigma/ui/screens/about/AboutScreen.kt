package com.example.enigma.ui.screens.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
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
import com.example.enigma.BuildConfig
import com.example.enigma.R
import com.example.enigma.ui.screens.common.StandardAppBar

@Composable
fun AboutScreen(
    navigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.gpl_license),
                style = MaterialTheme.typography.bodySmall,
            )

            Link(
                context = context,
                url = stringResource(R.string.gnu_license_link)
            )

            Text(
                text = stringResource(R.string.openssl_notice),
                style = MaterialTheme.typography.bodySmall
            )

            Link(
                context = context,
                url = stringResource(R.string.openssl_link)
            )

            Text(
                text = stringResource(R.string.eric_young_notice),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.source_code_available_at),
                style = MaterialTheme.typography.bodySmall,
            )

            Link(
                context = context,
                url = stringResource(R.string.source_code_link)
            )

            Text(
                text = stringResource(R.string.disclaimer),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun Link(
    context: Context,
    url: String
){
    TextButton(
        modifier = Modifier.padding(0.dp),
        onClick = {
            openLinkInBrowser(
                context = context,
                url = url
            )
        }
    ) {
        Text(
            text = url,
            fontSize = MaterialTheme.typography.bodySmall.fontSize
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
