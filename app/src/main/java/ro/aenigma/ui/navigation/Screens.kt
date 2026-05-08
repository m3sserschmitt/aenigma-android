package ro.aenigma.ui.navigation

import android.net.Uri
import androidx.navigation.NavController
import ro.aenigma.util.Constants.Companion.APP_DEEP_LINK_SCHEME
import ro.aenigma.util.Constants.Companion.PRIVACY_POLICY_URL_TEMPLATE
import ro.aenigma.util.QrCodeScannerState
import ro.aenigma.viewmodels.MainViewModel
import java.util.Locale

class Screens(navController: NavController, mainViewModel: MainViewModel) {

    companion object {
        const val URI_ARG = "uri"
        const val CHAT_ID_ARG = "chatId"
        const val CONTACT_ID_ARG = "contactId"
        const val SCANNER_STATE_ARG = "scannerState"
        const val MESSAGE_ID_ARG = "messageId"
        const val TITLE_ARG = "title"

        const val CONTACTS_ROOT_PATH = "contacts"
        const val CHAT_ROOT_PATH = "chat"
        const val ADD_CONTACTS_ROOT_PATH = "addContacts"
        const val ABOUT_ROOT_PATH = "about"
        const val LICENSES_ROOT_PATH = "licenses"
        const val FEED_ROOT_PATH = "feed"
        const val ARTICLE_ROOT_PATH = "article"

        const val CONTACTS_PATH =
            "$CONTACTS_ROOT_PATH?" +
                    "$URI_ARG={$URI_ARG}&" +
                    "$MESSAGE_ID_ARG={$MESSAGE_ID_ARG}"
        const val CHAT_PATH = "$CHAT_ROOT_PATH/{$CHAT_ID_ARG}"
        const val CHAT_DEEP_LINK = "$APP_DEEP_LINK_SCHEME://$CHAT_PATH"
        const val ADD_CONTACTS_PATH =
            "$ADD_CONTACTS_ROOT_PATH?" +
                    "$CONTACT_ID_ARG={$CONTACT_ID_ARG}&" +
                    "$URI_ARG={$URI_ARG}&" +
                    "$SCANNER_STATE_ARG={$SCANNER_STATE_ARG}"
        const val ABOUT_SCREEN_PATH = ABOUT_ROOT_PATH
        const val LICENSES_SCREEN_PATH = LICENSES_ROOT_PATH
        const val FEED_SCREEN_PATH = FEED_ROOT_PATH
        const val ARTICLE_SCREEN_PATH =
            "$ARTICLE_ROOT_PATH?" +
                    "$URI_ARG={$URI_ARG}&" +
                    "$TITLE_ARG={$TITLE_ARG}&" +
                    "$MESSAGE_ID_ARG={$MESSAGE_ID_ARG}"

        const val ROOT_PATH = CONTACTS_ROOT_PATH

        @JvmStatic
        fun getChatScreenRoute(chatId: String): String {
            return Uri.Builder().path(CHAT_ROOT_PATH).appendPath(chatId).build().toString()
        }

        @JvmStatic
        fun getChatDeepLink(chatId: String): Uri {
            return Uri.Builder().scheme(APP_DEEP_LINK_SCHEME).authority(CHAT_ROOT_PATH).appendPath(chatId).build()
        }

        @JvmStatic
        fun getAddContactsScreenRoute(
            contactId: String?,
            uri: String?,
            scannerState: QrCodeScannerState
        ): String {
            val builder = Uri.Builder()
                .path(ADD_CONTACTS_ROOT_PATH)
                .appendQueryParameter(SCANNER_STATE_ARG, scannerState.toString())
            contactId?.let { builder.appendQueryParameter(CONTACT_ID_ARG, it) }
            uri?.let { builder.appendQueryParameter(URI_ARG, it) }
            return builder.build().toString()
        }

        @JvmStatic
        fun getArticleScreenRoute(uri: String, title: String?, messageId: Long?): String {
            val builder = Uri.Builder()
                .path(ARTICLE_ROOT_PATH)
                .appendQueryParameter(URI_ARG, uri)
            messageId.let { builder.appendQueryParameter(MESSAGE_ID_ARG, it.toString()) }
            title.let { builder.appendQueryParameter(TITLE_ARG, it) }
            return builder.build().toString()
        }

        @JvmStatic
        fun getPrivacyPolicyScreenRoute(): String {
            val url = String.format(PRIVACY_POLICY_URL_TEMPLATE, Locale.getDefault().language)
            return getArticleScreenRoute(url, null, null)
        }

        @JvmStatic
        fun getContactsRoute(uri: String?, messageId: Long?): String {
            val builder = Uri.Builder().path(CONTACTS_ROOT_PATH)
            uri?.let { builder.appendQueryParameter(URI_ARG, it) }
            messageId.let { builder.appendQueryParameter(MESSAGE_ID_ARG, it.toString()) }
            return builder.build().toString()
        }
    }

    val root: () -> Unit = {
        navController.navigate(ROOT_PATH) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val contacts: () -> Unit = { navController.navigate(getContactsRoute(null, null)) }

    val back: () -> Unit = { navController.popBackStack() }

    val forwardUri: (uri: String) -> Unit =
        { uri -> navController.navigate(getContactsRoute(uri, null)) }

    val forwardMessage: (id: Long) -> Unit =
        { id -> navController.navigate(getContactsRoute(null, id)) }

    val chat: (chatId: String) -> Unit =
        { chatId -> navController.navigate(getChatScreenRoute(chatId)) }

    val addContacts: (contactId: String?) -> Unit = { contactId ->
        navController.navigate(
            getAddContactsScreenRoute(
                contactId = contactId,
                uri = null,
                scannerState = QrCodeScannerState.SHARE_CODE
            )
        )
    }

    val getSharedContact: (uri: String) -> Unit = { uri ->
        navController.navigate(
            getAddContactsScreenRoute(
                contactId = null,
                uri = uri,
                scannerState = QrCodeScannerState.SHARE_CODE
            )
        )
    }

    val scanServerCode: () -> Unit = {
        navController.navigate(
            getAddContactsScreenRoute(
                contactId = null,
                uri = null,
                scannerState = QrCodeScannerState.SCAN_SERVER_INFO_CODE
            )
        )
    }

    val about: () -> Unit = { navController.navigate(ABOUT_SCREEN_PATH) }

    val licenses: () -> Unit = { navController.navigate(LICENSES_SCREEN_PATH) }

    val feed: () -> Unit = {
        mainViewModel.resetFeedScroll()
        navController.navigate(FEED_SCREEN_PATH)
    }

    val article: (uri: String, title: String?, messageId: Long?) -> Unit =
        { uri, title, messageId ->
            navController.navigate(getArticleScreenRoute(uri, title, messageId))
        }

    val privacyPolicy: () -> Unit = { navController.navigate(getPrivacyPolicyScreenRoute()) }
}
