package com.example.enigma.ui.screens.contacts

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.R
import com.example.enigma.ui.screens.common.SimpleInfoScreen

@Composable
fun EmptySearchResult(
    modifier: Modifier = Modifier
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(
            id = R.string.no_contacts_found
        ),
        icon = {
            Icon(
                modifier = Modifier.size(120.dp),
                painter = painterResource(
                    id = R.drawable.ic_people
                ),
                contentDescription = stringResource(
                    id = R.string.no_contacts_found
                )
            )
        }
    )
}

@Preview
@Composable
fun EmptySearchResultPreview()
{
    EmptySearchResult()
}