package com.example.enigma.ui.navigation

import androidx.navigation.NavController
import com.example.enigma.util.Constants.Companion.CONTACTS_SCREEN

class Screens(navController: NavController) {
    val contacts: () -> Unit = {
        navController.navigate("contacts") {
            popUpTo(CONTACTS_SCREEN) { inclusive = true }
        }
    }

    val chat: (String) -> Unit = { chatId ->
        navController.navigate("chat/$chatId")
    }

    val addContact: () -> Unit = {
        navController.navigate("addContact")
    }
}
