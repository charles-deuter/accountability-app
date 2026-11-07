package dev.accountabilityapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RotationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun aTitleTypedIntoTheTaskForm_survivesRotation() {
    composeTestRule.onNodeWithContentDescription("Add task").performClick()
    composeTestRule.onNodeWithText("Title").performTextInput("Call dentist")

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.onNodeWithText("Call dentist").assertIsDisplayed()
  }
}
