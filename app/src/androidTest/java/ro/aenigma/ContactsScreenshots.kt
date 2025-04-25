package ro.aenigma

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import ro.aenigma.ui.screens.contacts.ContactsScreenDarkPreview
import ro.aenigma.ui.screens.contacts.ContactsScreenPreview

@RunWith(AndroidJUnit4::class)
class ContactsScreenshots: ScreenshotBase() {

    @Test
    fun captureContactsScreen() {
        return captureScreenshot("ContactsScreen") { ContactsScreenPreview() }
    }

    @Test
    fun captureContactsScreenDark() {
        return captureScreenshot("ContactsScreenDark") { ContactsScreenDarkPreview() }
    }
}
