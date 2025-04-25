package ro.aenigma

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Before
import org.junit.Rule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

open class ScreenshotBase {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun init() {
        Screengrab
            .setDefaultScreenshotStrategy(
                UiAutomatorScreenshotStrategy()
            )
    }

    fun captureScreenshot(imageName: String, composable: @Composable () -> Unit) {
        composeTestRule.setContent(composable)
        Thread.sleep(500)
        return Screengrab.screenshot(imageName)
    }
}
