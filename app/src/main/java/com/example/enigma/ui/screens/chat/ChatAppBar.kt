package com.example.enigma.ui.screens.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.util.DatabaseRequestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppBar(
    contact: DatabaseRequestState<ContactEntity>,
    navigateToContactsScreen: () -> Unit
){
    TopAppBar(
        navigationIcon = {
            BackAction(
                onBackClicked = navigateToContactsScreen
            )
        },
        title = {
            Text(
                text = if(contact is DatabaseRequestState.Success) contact.data.name else "",
                // TODO color = MaterialTheme.colorScheme.topAppBarContentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 24.sp
            )
        }
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
fun ChatAppBarPreview()
{
    ChatAppBar(
        contact = DatabaseRequestState.Success(ContactEntity(
            "123456-5678-5678-123456",
            "John",
            "public-key",
            "guard-hostname",
            true
        )),
        navigateToContactsScreen = {}
    )
}