package com.example.enigma.ui.screens.addContacts

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.ui.screens.common.NavigateBackAppBarAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactsAppBar(
    navigateToContactsScreen: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            NavigateBackAppBarAction(
                onBackClicked = navigateToContactsScreen
            )
        },
        title = {
            Text(
                text = ""
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.Transparent
        )
    )
}

@Composable
@Preview
fun AddContactsAppBarPreview()
{
    AddContactsAppBar(
        navigateToContactsScreen = {}
    )
}
