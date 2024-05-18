package com.example.enigma.ui.navigation

import androidx.navigation.NavController

class Screens(navController: NavController) {

    companion object
    {
        const val CONTACTS_SCREEN = "contacts"
        const val CHAT_SCREEN_CHAT_ID_ARG = "{chatId}"
        const val CHAT_SCREEN = "chat/$CHAT_SCREEN_CHAT_ID_ARG"
        const val ADD_CONTACT_SCREEN = "addContact"
        const val STARTING_SCREEN = CONTACTS_SCREEN
        const val NO_SCREEN = "none"

        @JvmStatic
        fun getChatScreenRoute(chatId: String): String
        {
            return CHAT_SCREEN.replace(CHAT_SCREEN_CHAT_ID_ARG, chatId)
        }

        @JvmStatic
        fun getChatIdFromChatRoute(chatRoute: String): String? {
            val regex = Regex("chat/([a-fA-F0-9]{64})")
            val matchResult = regex.find(chatRoute)
            return matchResult?.groupValues?.get(1)
        }

    }

    val contacts: () -> Unit = {
        navController.navigate(CONTACTS_SCREEN) {
            popUpTo(CONTACTS_SCREEN) { inclusive = true }
        }
    }

    val chat: (String) -> Unit = {
        chatId -> navController.navigate(getChatScreenRoute(chatId))
    }

    val addContact: () -> Unit = {
        navController.navigate(ADD_CONTACT_SCREEN)
    }
}
