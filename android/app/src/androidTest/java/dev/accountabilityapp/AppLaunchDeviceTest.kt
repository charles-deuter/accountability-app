package dev.accountabilityapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Same behaviour as the JVM [AppLaunchTest], run on a real device or emulator. */
@RunWith(AndroidJUnit4::class)
class AppLaunchDeviceTest {

  @get:Rule(order = 0) val onboardingComplete = OnboardingCompleteRule()

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun launchingTheApp_showsTheAppName() {
    composeTestRule.onNodeWithText("Accountability App").assertIsDisplayed()
  }
}
