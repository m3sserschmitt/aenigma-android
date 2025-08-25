package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.services.NavigationTracker
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.navigation.Screens.Companion.getArticleScreenRoute
import ro.aenigma.ui.screens.feed.ArticleScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.articleComposable (
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToFeed: () -> Unit
) {
    composable(
        route = Screens.ARTICLE_SCREEN_ROUTE_FULL,
        arguments = listOf(
            navArgument(Screens.ARTICLE_SCREEN_ARTICLE_URL_ARG)
            {
                type = NavType.StringType
            })
    ) { navBackStackEntry ->
        val url = navBackStackEntry.arguments!!.getString(Screens.ARTICLE_SCREEN_ARTICLE_URL_ARG)
        LaunchedEffect(key1 = true) {
            if (!url.isNullOrBlank()) {
                navigationTracker.postCurrentRoute(getArticleScreenRoute(url))
            }
        }

        ArticleScreen(
            url = url,
            mainViewModel = mainViewModel,
            navigateBack = navigateToFeed,
        )
    }
}
