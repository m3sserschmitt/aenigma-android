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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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

    open fun setContent(composable: @Composable () -> Unit) {
        return composeTestRule.setContent(composable)
    }

    open fun waitForIdle() {
        return composeTestRule.waitForIdle()
    }

    open fun takeScreenShot(imageName: String) {
        return Screengrab.screenshot(imageName)
    }

    fun captureScreenshot(imageName: String, composable: @Composable () -> Unit) {
        setContent(composable)
        waitForIdle()
        takeScreenShot(imageName)
    }
}
