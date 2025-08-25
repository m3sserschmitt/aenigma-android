package ro.aenigma.util

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import ro.aenigma.ui.navigation.Screens
import kotlin.sequences.any

object NavBackStackEntryExtensions {
    fun NavBackStackEntry?.isContactsSelected(): Boolean {
        return this?.destination?.hierarchy?.any { destination ->
            (destination.route?.startsWith(Screens.CONTACTS_SCREEN_BASE_ROUTE) ?: false)
                    || (destination.route?.startsWith(Screens.ADD_CONTACTS_BASE_ROUTE) ?: false)
                    || (destination.route?.startsWith(Screens.CHAT_SCREEN_BASE_ROUTE) ?: false)
        } == true
    }

    fun NavBackStackEntry?.isFeedSelected(): Boolean {
        return this?.destination?.hierarchy?.any { destination ->
            (destination.route?.startsWith(Screens.FEED_SCREEN_ROUTE_FULL) ?: false)
                    || (destination.route?.startsWith(Screens.ARTICLE_BASE_ROUTE) ?: false)
        } == true
    }
}
