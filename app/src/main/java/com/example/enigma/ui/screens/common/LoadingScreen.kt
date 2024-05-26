package com.example.enigma.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(
            id = R.string.loading
        ),
        icon = {
            IndeterminateCircularIndicator(
                visible = true,
                text = ""
            )
        }
    )
}

@Preview
@Composable
fun LoadingScreenPreview()
{
    LoadingScreen()
}