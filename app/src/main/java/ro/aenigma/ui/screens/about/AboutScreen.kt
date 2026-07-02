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

package ro.aenigma.ui.screens.about

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.BuildConfig
import ro.aenigma.R
import ro.aenigma.ui.screens.common.DialogContentTemplate
import ro.aenigma.ui.screens.common.StandardAppBar
import androidx.core.net.toUri

@Composable
fun AboutScreen(
    navigateBack: () -> Unit,
    navigateToLicensesScreen: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit
) {
    var appLicenseVisible by remember { mutableStateOf(false) }
    var apacheLicenseVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LicenseDialog(
        visible = appLicenseVisible,
        title = stringResource(id = R.string.gnu_gpl_v3_0_title),
        subtitle = "",
        text = readRawTextResource(context, R.raw.gnu_gpl_v3_0_license),
        onCloseButtonClicked = {
            appLicenseVisible = false
        }
    )

    LicenseDialog(
        visible = apacheLicenseVisible,
        title = stringResource(id = R.string.apache_v2_0_license_title),
        subtitle = "",
        text = readRawTextResource(context, R.raw.apache_v2_0_license),
        onCloseButtonClicked = {
            apacheLicenseVisible = false
        }
    )

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
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
                    bottom = padding.calculateBottomPadding(),
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
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.copyright_notices),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.gpl_license_info),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Link(
                context = context,
                url = stringResource(id = R.string.gnu_gpl_v3_0_license),
                action = {
                    appLicenseVisible = true
                }
            )

            Text(
                text = stringResource(R.string.source_code_available_at),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Link(
                context = context,
                url = stringResource(R.string.source_code_link)
            )

            Text(
                text = stringResource(R.string.disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Link(
                context = context,
                url = stringResource(id = R.string.open_source_libraries),
                action = navigateToLicensesScreen,
            )

            Text(
                text = stringResource(R.string.backed_by_openssl),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Link(
                context = context,
                url = stringResource(id = R.string.apache_v2_0_license),
                action = {
                    apacheLicenseVisible = true
                }
            )

            Link(
                context = context,
                url = stringResource(id = R.string.privacy_policy),
                action = navigateToPrivacyPolicy
            )
            VerticalDivider(
                modifier = Modifier.weight(1f)
                    .background(color = MaterialTheme.colorScheme.background),
                color = MaterialTheme.colorScheme.background
            )
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = stringResource(R.string.made_with_love),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun Link(
    context: Context,
    url: String = "",
    action: () -> Unit = {
        openLinkInBrowser(
            context = context,
            url = url
        )
    }
) {
    TextButton(
        modifier = Modifier.padding(0.dp),
        onClick = action,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = url,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseDialog(
    visible: Boolean,
    title: String,
    subtitle: String,
    text: String,
    onCloseButtonClicked: () -> Unit
) {
    val scrollState = rememberScrollState()
    if (visible ) {
        BasicAlertDialog(
            onDismissRequest = { }
        ) {
            DialogContentTemplate(
                title = title,
                body = subtitle,
                dismissible = false,
                onNegativeButtonClicked = onCloseButtonClicked,
                onPositiveButtonClicked = onCloseButtonClicked,
                content = {
                    BasicText(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .fillMaxHeight(fraction = .75f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    )
                }
            )
        }
    }
}

fun openLinkInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}

fun readRawTextResource(
    context: Context,
    resourceId: Int
): String {
    val inputStream = context.resources.openRawResource(resourceId)
    return inputStream.bufferedReader().use { it.readText() }
}

@Preview
@Composable
fun AboutScreenPreview()
{
    AboutScreen(
        navigateBack = { },
        navigateToLicensesScreen = { },
        navigateToPrivacyPolicy = { }
    )
}

@Preview
@Composable
fun LicenseDialogPreview()
{
    val context = LocalContext.current
    LicenseDialog(
        visible = true,
        title = stringResource(id = R.string.gnu_gpl_v3_0_title),
        subtitle = "",
        text = readRawTextResource(context, R.raw.gnu_gpl_v3_0_license),
        onCloseButtonClicked = { }
    )
}
