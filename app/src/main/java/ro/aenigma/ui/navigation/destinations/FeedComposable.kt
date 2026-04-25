package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.NotificationService
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.feed.FeedScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.feedComposable (
    notificationService: NotificationService,
    mainViewModel: MainViewModel,
    navigateToArticle: (uri: String, title: String?, messageId: Long?) -> Unit,
    redirectUri: (String) -> Unit
) {
    composable(
        route = Screens.FEED_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notificationService.exitChat()
            notificationService.enableNotifications()
        }

        FeedScreen(
            mainViewModel = mainViewModel,
            navigateToArticle = navigateToArticle,
            redirectUri = redirectUri
        )
    }
}
