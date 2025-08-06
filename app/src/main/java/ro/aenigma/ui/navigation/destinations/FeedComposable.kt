package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.NavigationTracker
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.feed.FeedScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.feedComposable (
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToArticle: (String) -> Unit
) {
    composable(
        route = Screens.FEED_SCREEN_ROUTE_FULL
    ) {
        LaunchedEffect(key1 = true) {
            navigationTracker.postCurrentRoute(Screens.FEED_SCREEN_ROUTE_FULL)
        }

        FeedScreen(
            mainViewModel = mainViewModel,
            navigateToArticle = navigateToArticle
        )
    }
}
