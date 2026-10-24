package dev.accountabilityapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskEndToEndTest {

  @get:Rule val composeTestRule = createEmptyComposeRule()

  private lateinit var scenario: ActivityScenario<MainActivity>

  @Before
  fun launchWithAnEmptyTaskList() {
    ApplicationProvider.getApplicationContext<AccountabilityApplication>().container.database.clearAllTables()
    scenario = ActivityScenario.launch(MainActivity::class.java)
  }

  @After
  fun closeActivity() {
    scenario.close()
  }

  @Test
  fun addingATask_showsItInTheTaskList() {
    composeTestRule.onNodeWithContentDescription("Add task").performClick()
    composeTestRule.onNodeWithText("Title").performTextInput("Call dentist")
    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.onNodeWithText("Call dentist").assertIsDisplayed()
  }

  @Test
  fun addingTwoTasksInARow_showsBoth() {
    addTask("Call dentist")
    addTask("Pay rent")

    composeTestRule.onNodeWithText("Call dentist").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pay rent").assertIsDisplayed()
  }

  @Test
  fun tappingATask_opensItPrefilled_andSavingShowsTheEdit() {
    addTask("Call dentist")

    composeTestRule.onNodeWithText("Call dentist").performClick()
    composeTestRule.onNodeWithText("Call dentist").performTextReplacement("Call the dentist back")
    composeTestRule.onNodeWithText("Save").performClick()

    composeTestRule.onNodeWithText("Call the dentist back").assertIsDisplayed()
    composeTestRule.onNodeWithText("Call dentist").assertDoesNotExist()
  }

  @Test
  fun deletingATask_asksForConfirmation_thenRemovesIt() {
    addTask("Call dentist")

    composeTestRule.onNodeWithText("Call dentist").performClick()
    composeTestRule.onNodeWithContentDescription("Delete").performClick()
    composeTestRule.onNodeWithText("Delete this reminder?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete").performClick()

    composeTestRule.onNodeWithText("No reminders yet. Tap + to add one.").assertIsDisplayed()
  }

  private fun addTask(title: String) {
    composeTestRule.onNodeWithContentDescription("Add task").performClick()
    composeTestRule.onNodeWithText("Title").performTextInput(title)
    composeTestRule.onNodeWithText("Save").performClick()
  }
}
