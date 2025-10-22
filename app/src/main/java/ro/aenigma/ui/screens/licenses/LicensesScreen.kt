package ro.aenigma.ui.screens.licenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import ro.aenigma.R
import ro.aenigma.ui.screens.common.StandardAppBar

@Composable
fun LicensesScreen(
    navigateBack: () -> Unit
) {
    val libraries by produceLibraries(R.raw.aboutlibraries)
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        topBar = {
            StandardAppBar(
                title = stringResource(id = R.string.open_source_libraries),
                navigateBack = navigateBack
            )
        }
    ) { padding ->
        LibrariesContainer(
            modifier = Modifier.fillMaxSize().padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
                start = 8.dp,
                end = 8.dp
            ),
            libraries = libraries
        )
    }
}
