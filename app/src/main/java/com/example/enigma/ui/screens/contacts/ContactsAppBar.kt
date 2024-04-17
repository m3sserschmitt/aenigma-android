package com.example.enigma.ui.screens.contacts

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.ui.screens.common.ActivateSearchAppBarAction
import com.example.enigma.ui.screens.common.DeleteAppBarAction
import com.example.enigma.ui.screens.common.EditTopAppBarAction
import com.example.enigma.ui.screens.common.SearchAppBar
import com.example.enigma.ui.screens.common.SelectionModeAppBar

@Composable
fun ContactsAppBar(
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    selectedItemsCount: Int,
    onSearchTriggered: () -> Unit,
    onSearchDeactivated: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onSelectionModeExited: () -> Unit,
    onDeleteSelectedItemsClicked: () -> Unit,
    onRenameSelectedItemClicked: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    if(isSelectionMode)
    {
        SelectionModeAppBar(
            selectedItemsCount = selectedItemsCount,
            onSelectionModeExited = onSelectionModeExited,
            actions = {
                DeleteAppBarAction(
                    onDeleteClicked = onDeleteSelectedItemsClicked
                )
                if(selectedItemsCount == 1)
                {
                    EditTopAppBarAction(
                        onRenameClicked = onRenameSelectedItemClicked
                    )
                }
            }
        )
    } else if (isSearchMode){
        SearchAppBar(
            text = text,
            onTextChanged = {
                    newSearchText -> text = newSearchText
            },
            onClose = onSearchDeactivated,
            onSearchClicked = {
                searchQuery -> onSearchClicked(searchQuery)
            }
        )
    } else {
        DefaultContactsAppBar(
            onSearchTriggered = onSearchTriggered
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultContactsAppBar(
    onSearchTriggered: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                maxLines = 1,
                text = stringResource(
                    id = R.string.contacts
                ),
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )
        },
        actions = {
            ActivateSearchAppBarAction(
                onSearchModeTriggered = onSearchTriggered
            )
        }
    )
}

@Composable
@Preview
private fun DefaultContactsAppBarPreview()
{
    DefaultContactsAppBar(
        onSearchTriggered = {}
    )
}

