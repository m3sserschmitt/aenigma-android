package com.example.enigma.ui.screens.contacts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.ui.themes.TOP_APP_BAR_HEIGHT
import com.example.enigma.util.SearchAppBarCloseButtonState
import com.example.enigma.util.SearchAppBarState
import com.example.enigma.viewmodels.MainViewModel

@Composable
fun ContactsAppBar(
    mainViewModel: MainViewModel,
    searchAppBarState: SearchAppBarState,
    searchTextState: String,
)
{
    when(searchAppBarState)
    {
        SearchAppBarState.CLOSED -> {
            DefaultContactsAppBar(
                onSearchClicked = {
                    mainViewModel.searchAppBarState.value = SearchAppBarState.OPENED
                }
            )
        }
        else -> {
            SearchAppBar(
                text = searchTextState,
                onTextChanged = {
                    newText -> mainViewModel.contactsSearch.value = newText
                },
                onCloseClicked = {
                    mainViewModel.searchAppBarState.value = SearchAppBarState.CLOSED
                    mainViewModel.contactsSearch.value = ""
                },
                onSearchClicked = {}
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultContactsAppBar(
    onSearchClicked: () -> Unit
)
{
    TopAppBar(
        title = {
            Text (
                text = stringResource(
                    id = R.string.contacts
                ),
                // TODO color = MaterialTheme.colorScheme.topAppBarContentColor
            )
        },
        actions = {
            ContactsAppBarActions(
                onSearchClicked = onSearchClicked
            )
        }
    )
}

@Composable
fun ContactsAppBarActions(
    onSearchClicked: () -> Unit
)
{
    SearchAction(
        onSearchClicked = onSearchClicked
    )
}

@Composable
fun SearchAction(
    onSearchClicked: () -> Unit
)
{
    IconButton(onClick = {
        onSearchClicked()
    }) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource (
                id = R.string.search_contacts
            ),
            // TODO tint = MaterialTheme.colorScheme.topAppBarContentColor
        )
    }
}

@Composable
fun SearchAppBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit
)
{
    var closeButtonState: SearchAppBarCloseButtonState by remember {
        mutableStateOf(SearchAppBarCloseButtonState.READY_TO_CLOSE)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(TOP_APP_BAR_HEIGHT),
        // TODO color = MaterialTheme.colorScheme.topAppBarBackgroundColor
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = {
                text -> onTextChanged(text)
            },
            placeholder = {
                Text(
                    modifier = Modifier.alpha(0.5f),
                    text = stringResource(id = R.string.search),
                    // TODO color = MaterialTheme.colorScheme.topAppBarContentColor
                )
            },
            singleLine = true,
            leadingIcon = {
                IconButton(
                    modifier = Modifier.alpha(0.5f),
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(id = R.string.search),
                        // TODO tint = MaterialTheme.colorScheme.topAppBarContentColor
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        when(closeButtonState)
                        {
                            SearchAppBarCloseButtonState.READY_TO_DELETE -> {
                                onTextChanged("")
                                closeButtonState = SearchAppBarCloseButtonState.READY_TO_CLOSE
                            }
                            SearchAppBarCloseButtonState.READY_TO_CLOSE -> {
                                if(text.isNotEmpty())
                                {
                                    onTextChanged("")
                                } else
                                {
                                    onCloseClicked()
                                    closeButtonState = SearchAppBarCloseButtonState.READY_TO_DELETE
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.close),
                        // TODO tint = MaterialTheme.colorScheme.topAppBarContentColor
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchClicked(text)
                }
            ),
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
@Preview
private fun DefaultContactsAppBarPreview()
{
    DefaultContactsAppBar(
        onSearchClicked = {}
    )
}

@Composable
@Preview
private fun SearchAppBarPreview()
{
    SearchAppBar(
        text = "",
        onTextChanged = {},
        onCloseClicked = {},
        onSearchClicked = {}
    )
}