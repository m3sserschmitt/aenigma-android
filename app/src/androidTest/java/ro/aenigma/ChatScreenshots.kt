package ro.aenigma

import org.junit.Test
import ro.aenigma.ui.screens.chat.ChatScreenDarkPreview
import ro.aenigma.ui.screens.chat.ChatScreenPreview

class ChatScreenshots: ScreenshotBase() {

    @Test
    fun captureChatScreen() {
        return captureScreenshot("ChatScreen") { ChatScreenPreview() }
    }

    @Test
    fun captureChatScreenDark() {
        return captureScreenshot("ChatScreenDark") { ChatScreenDarkPreview() }
    }
}
