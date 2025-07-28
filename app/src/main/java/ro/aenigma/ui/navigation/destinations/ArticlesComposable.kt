package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.NavigationTracker
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.feed.ArticlesScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.articlesComposable (
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel
) {
    composable(
        route = Screens.ARTICLES_SCREEN_ROUTE_FULL
    ) {
        LaunchedEffect(key1 = true) {
            navigationTracker.postCurrentRoute(Screens.ARTICLES_SCREEN_ROUTE_FULL)
        }

        ArticlesScreen(
            mainViewModel = mainViewModel
        )
    }
}
