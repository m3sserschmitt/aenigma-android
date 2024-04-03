package com.example.enigma.ui.screens.contacts

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.enigma.R
import com.example.enigma.util.SearchAppBarState
import com.example.enigma.viewmodels.MainViewModel

@Composable
fun ContactsScreen(
    navigateToChatScreen: (String) -> Unit,
    navigateToAddContactScreen: () -> Unit,
    mainViewModel: MainViewModel
)
{
    LaunchedEffect(key1 = true)
    {
        mainViewModel.getAllContacts()
    }

    val allContacts by mainViewModel.allContacts.collectAsState()
    val searchAppBarState: SearchAppBarState by mainViewModel.searchAppBarState
    val searchTextState: String by mainViewModel.contactsSearch

    Scaffold(
        topBar = {
            ContactsAppBar(
                mainViewModel = mainViewModel,
                searchAppBarState = searchAppBarState,
                searchTextState = searchTextState
            )
        },
        content = { paddingValues ->
            ContactsListContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
                contacts = allContacts,
                navigateToChatScreen = navigateToChatScreen
            )
        },
        floatingActionButton = {
            ContactsFab(
                onFabClicked = navigateToAddContactScreen
            )
        }
    )
}

@Composable
fun ContactsFab(
    onFabClicked: () -> Unit
)
{
    FloatingActionButton(
        onClick = { onFabClicked() },
    ) {
        Icon (
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource (
                id = R.string.contacts_floating_button_content_description
            ),
            tint = Color.White
        )
    }
}
