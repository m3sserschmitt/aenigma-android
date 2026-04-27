package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.feed.FeedScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.feedComposable (
    notifier: Notifier,
    mainViewModel: MainViewModel,
    navigateToArticle: (uri: String, title: String?, messageId: Long?) -> Unit,
    redirectUri: (String) -> Unit
) {
    composable(
        route = Screens.FEED_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notifier.exitChat()
            notifier.enableNotifications()
        }

        FeedScreen(
            mainViewModel = mainViewModel,
            navigateToArticle = navigateToArticle,
            redirectUri = redirectUri
        )
    }
}
