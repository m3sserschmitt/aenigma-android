package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onClose: () -> Unit,
    onSearchClicked: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = true)
    {
        focusRequester.requestFocus()
    }

    TopAppBar(
        navigationIcon = {
            IconButton(
                modifier = Modifier.alpha(0.5f),
                onClick = {
                    onSearchClicked(searchQuery)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.search)
                )
            }
        },
        title = {
            TextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                value = searchQuery,
                onValueChange = { text ->
                    onSearchQueryChanged(text)
                },
                placeholder = {
                    Text(
                        modifier = Modifier.alpha(0.5f),
                        text = stringResource(id = R.string.search)
                    )
                },
                singleLine = true,
                trailingIcon = {

                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClicked(searchQuery)
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
        },
        actions = {
            CloseSearchTopAppBarAction(
                isEmptySearchQuery = searchQuery.isEmpty(),
                onClose = onClose,
                onClearSearchQuery = {
                    onSearchQueryChanged("")
                }
            )
        }
    )
}

@Composable
@Preview
private fun SearchAppBarPreview()
{
    SearchAppBar(
        searchQuery = "John",
        onSearchQueryChanged = {},
        onClose = {},
        onSearchClicked = {},
    )
}
