/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.ui.screens.feed

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.models.NewPostSheetStateDto
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toExpanded
import ro.aenigma.models.extensions.NewPostSheetStateDtoExtensions.ServersSheetStateDtoExtensions.toPartiallyExpanded
import ro.aenigma.models.factories.NewPostSheetStateDtoFactory
import ro.aenigma.ui.screens.common.ComposeNewArticleAppBarAction
import ro.aenigma.ui.screens.common.ReloadAppBarAction
import ro.aenigma.ui.screens.common.ShowInfoAppBarAction
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.BottomSheetScaffoldStateExtensions.isNotFullyExpanded

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedAppBar(
    newPostSheetState: NewPostSheetStateDto = NewPostSheetStateDtoFactory.create(),
    onReloadFeedClicked: () -> Unit = { },
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    onNewPostSheetStateChanged: (NewPostSheetStateDto) -> Unit = { },
    navigateToFeedHelpScreen: () -> Unit = { }
) {
    StandardAppBar(
        title = stringResource(id = R.string.news),
        navigateBackVisible = false,
        actions = {
            ReloadAppBarAction(
                visible = true,
                tint = MaterialTheme.colorScheme.onBackground,
                onClick = onReloadFeedClicked
            )
            ShowInfoAppBarAction(
                tint = MaterialTheme.colorScheme.onBackground,
                onShowInfoClicked = navigateToFeedHelpScreen
            )
        },
        navigateBackAlternative = {
            ComposeNewArticleAppBarAction(
                tint = MaterialTheme.colorScheme.onBackground,
                onComposeNewArticle = {
                    if (bottomSheetScaffoldState.isNotFullyExpanded()) {
                        onNewPostSheetStateChanged(newPostSheetState.toExpanded())
                    } else {
                        onNewPostSheetStateChanged(newPostSheetState.toPartiallyExpanded())
                    }
                }
            )
        }
    )
}
