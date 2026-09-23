/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2023-2026 Romulus-Emanuel Ruja <romulus.ruja@aenigma.ro>

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

package ro.aenigma

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import ro.aenigma.ui.screens.feed.FeedScreenStoryBottomSheetDarkPreview
import ro.aenigma.ui.screens.feed.FeedScreenStoryBottomSheetPreview

@RunWith(AndroidJUnit4::class)
class FeedStoryBottomSheetScreenshots: ScreenshotBase() {

    @Test
    fun captureFeedScreenStoryBottomSheet() {
        return captureScreenshot("FeedScreenStoryBottomSheet") { FeedScreenStoryBottomSheetPreview() }
    }

    @Test
    fun captureFeedScreenStoryBottomSheetDark() {
        return captureScreenshot("FeedScreenStoryBottomSheetDark") { FeedScreenStoryBottomSheetDarkPreview() }
    }
}
