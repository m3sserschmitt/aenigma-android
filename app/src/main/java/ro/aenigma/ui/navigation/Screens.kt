package ro.aenigma.ui.navigation

import android.net.Uri
import androidx.navigation.NavController

class Screens(navController: NavController) {

    companion object {
        const val CHAT_SCREEN_CHAT_ID_ARG = "chatId"
        const val ADD_CONTACTS_SCREEN_CONTACT_ID_ARG = "contactId"
        const val ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE = "me"
        const val ARTICLE_SCREEN_ARTICLE_URL_ARG = "url"

        const val CONTACTS_SCREEN_BASE_ROUTE = "contacts"
        const val CHAT_SCREEN_BASE_ROUTE = "chat"
        const val ADD_CONTACTS_BASE_ROUTE = "addContacts"
        const val ABOUT_BASE_ROUTE = "about"
        const val LICENSES_BASE_ROUTE = "licenses"
        const val FEED_BASE_ROUTE = "feed"
        const val ARTICLE_BASE_ROUTE = "article"

        const val CONTACTS_SCREEN_ROUTE_FULL = CONTACTS_SCREEN_BASE_ROUTE
        const val CHAT_SCREEN_ROUTE_FULL = "$CHAT_SCREEN_BASE_ROUTE/{$CHAT_SCREEN_CHAT_ID_ARG}"
        const val ADD_CONTACT_SCREEN_ROUTE_FULL =
            "$ADD_CONTACTS_BASE_ROUTE/{$ADD_CONTACTS_SCREEN_CONTACT_ID_ARG}"
        const val ABOUT_SCREEN_ROUTE_FULL = ABOUT_BASE_ROUTE
        const val LICENSES_SCREEN_ROUTE_FULL = LICENSES_BASE_ROUTE
        const val FEED_SCREEN_ROUTE_FULL = FEED_BASE_ROUTE
        const val ARTICLE_SCREEN_ROUTE_FULL =
            "$ARTICLE_BASE_ROUTE/{$ARTICLE_SCREEN_ARTICLE_URL_ARG}"

        const val STARTING_SCREEN = CONTACTS_SCREEN_ROUTE_FULL
        const val NO_SCREEN = "none"

        @JvmStatic
        fun getChatScreenRoute(chatId: String): String {
            return CHAT_SCREEN_ROUTE_FULL.replace("{$CHAT_SCREEN_CHAT_ID_ARG}", chatId)
        }

        @JvmStatic
        fun getAddContactsScreenRoute(contactId: String?): String {
            return if (contactId.isNullOrBlank())
                ADD_CONTACT_SCREEN_ROUTE_FULL.replace(
                    "{$ADD_CONTACTS_SCREEN_CONTACT_ID_ARG}",
                    ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE
                )
            else
                ADD_CONTACT_SCREEN_ROUTE_FULL.replace(
                    "{$ADD_CONTACTS_SCREEN_CONTACT_ID_ARG}",
                    contactId
                )
        }

        @JvmStatic
        fun getArticleScreenRoute(url: String): String {
            val encodedUrl = Uri.encode(url).toString()
            return ARTICLE_SCREEN_ROUTE_FULL.replace("{$ARTICLE_SCREEN_ARTICLE_URL_ARG}", encodedUrl)
        }

        @JvmStatic
        fun getChatIdFromChatRoute(chatRoute: String): String? {
            val regex = Regex("chat/([a-fA-F0-9]{64})")
            val matchResult = regex.find(chatRoute)
            return matchResult?.groupValues?.get(1)
        }

    }

    val contacts: () -> Unit = {
        navController.navigate(CONTACTS_SCREEN_ROUTE_FULL) {
            popUpTo(CONTACTS_SCREEN_ROUTE_FULL) { inclusive = true }
        }
    }

    val chat: (String) -> Unit = { chatId ->
        navController.navigate(getChatScreenRoute(chatId))
    }

    val addContact: (String?) -> Unit = { contactId ->
        navController.navigate(getAddContactsScreenRoute(contactId))
    }

    val about: () -> Unit = {
        navController.navigate(ABOUT_SCREEN_ROUTE_FULL)
    }

    val licenses: () -> Unit = {
        navController.navigate(LICENSES_SCREEN_ROUTE_FULL)
    }

    val feed: () -> Unit = {
        navController.navigate(FEED_SCREEN_ROUTE_FULL)
    }

    val article: (String) -> Unit = { url ->
        navController.navigate(getArticleScreenRoute(url))
    }
}
