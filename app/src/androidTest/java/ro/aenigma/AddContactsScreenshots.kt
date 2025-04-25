package ro.aenigma

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import ro.aenigma.ui.screens.addContacts.AddContactsScreenDarkPreview
import ro.aenigma.ui.screens.addContacts.AddContactsScreenPreview

@RunWith(AndroidJUnit4::class)
class AddContactsScreenshots: ScreenshotBase() {

    @Test
    fun captureAddContactsScreen() {
        return captureScreenshot("AddContactsScreen") { AddContactsScreenPreview() }
    }

    @Test
    fun captureAddContactsScreenDark() {
        return captureScreenshot("AddContactsScreenDark") { AddContactsScreenDarkPreview() }
    }
}
