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


package ro.aenigma

import org.junit.Test
import ro.aenigma.ui.screens.chat.ChatScreenDarkPreview
import ro.aenigma.ui.screens.chat.ChatScreenMoreOptionsMenuExpandedDarkPreview
import ro.aenigma.ui.screens.chat.ChatScreenMoreOptionsMenuExpandedPreview
import ro.aenigma.ui.screens.chat.ChatScreenPreview
import ro.aenigma.ui.screens.chat.ChatScreenSelectionModeDarkPreview
import ro.aenigma.ui.screens.chat.ChatScreenSelectionModePreview

class ChatScreenshots: ScreenshotBase() {

    @Test
    fun captureChatScreen() {
        return captureScreenshot("ChatScreen") { ChatScreenPreview() }
    }

    @Test
    fun captureChatScreenSelectionMode() {
        return captureScreenshot("ChatScreenSelectionMode") { ChatScreenSelectionModePreview() }
    }

    @Test
    fun captureChatScreenMoreOptionsMenuExpanded() {
        return captureScreenshot("ChatScreenMoreOptionsMenuExpanded") { ChatScreenMoreOptionsMenuExpandedPreview() }
    }

    @Test
    fun captureChatScreenDark() {
        return captureScreenshot("ChatScreenDark") { ChatScreenDarkPreview() }
    }

    @Test
    fun captureChatScreenSelectionModeDark() {
        return captureScreenshot("ChatScreenSelectionModeDark") { ChatScreenSelectionModeDarkPreview() }
    }

    @Test
    fun captureChatScreenMoreOptionsMenuExpandedDark() {
        return captureScreenshot("ChatScreenMoreOptionsMenuExpandedDark") { ChatScreenMoreOptionsMenuExpandedDarkPreview() }
    }
}
