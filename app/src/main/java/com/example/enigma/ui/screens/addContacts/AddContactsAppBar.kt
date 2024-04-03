package com.example.enigma.ui.screens.addContacts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactsAppBar(
    navigateToContactsScreen: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            BackAction(
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
fun BackAction(
    onBackClicked: () -> Unit
) {
    IconButton(
        onClick = {
            onBackClicked()
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back),
        )
    }
}

@Composable
@Preview
fun AddContactsAppBarPreview()
{
    AddContactsAppBar(
        navigateToContactsScreen = {}
    )
}
