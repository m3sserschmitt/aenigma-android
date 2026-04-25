package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.services.NotificationService
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.feed.ArticleScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.articleComposable (
    notificationService: NotificationService,
    mainViewModel: MainViewModel,
    forwardMessage: (Long) -> Unit,
    navigateBack: () -> Unit
) {
    composable(
        route = Screens.ARTICLE_SCREEN_PATH,
        arguments = listOf(
            navArgument(Screens.URI_ARG)
            {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Screens.TITLE_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Screens.MESSAGE_ID_ARG) {
                type = NavType.LongType
                defaultValue = Long.MIN_VALUE
            }
        )
    ) { navBackStackEntry ->
        val uri = navBackStackEntry.arguments?.getString(Screens.URI_ARG)?.takeIf { t -> t.isNotBlank() }
        val title = navBackStackEntry.arguments?.getString(Screens.TITLE_ARG)?.takeIf { t -> t.isNotBlank() }
        val messageId = navBackStackEntry.arguments?.getLong(Screens.MESSAGE_ID_ARG)?.takeIf { id -> id > 0 }

        LaunchedEffect(key1 = true) {
            notificationService.exitChat()
            notificationService.enableNotifications()
        }

        ArticleScreen(
            uri = uri,
            title = title,
            messageId = messageId,
            mainViewModel = mainViewModel,
            forwardMessage = forwardMessage,
            navigateBack = navigateBack,
        )
    }
}
