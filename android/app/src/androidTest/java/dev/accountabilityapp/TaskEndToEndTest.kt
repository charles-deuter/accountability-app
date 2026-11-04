package dev.accountabilityapp

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
    val application = ApplicationProvider.getApplicationContext<AccountabilityApplication>()
    application.container.database.clearAllTables()
    // Grant up front, so the Task Form's permission dialog doesn't cover it.
    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    if (needsNotificationPermission) {
      val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
      uiAutomation.grantRuntimePermission(application.packageName, Manifest.permission.POST_NOTIFICATIONS)
    }
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

    waitForText("Call dentist")
    composeTestRule.onNodeWithText("Call dentist").assertIsDisplayed()
  }

  @Test
  fun addingTwoTasksInARow_showsBoth() {
    addTask("Call dentist")
    addTask("Pay rent")

    waitForText("Call dentist")
    composeTestRule.onNodeWithText("Call dentist").assertIsDisplayed()
    waitForText("Pay rent")
    composeTestRule.onNodeWithText("Pay rent").assertIsDisplayed()
  }

  @Test
  fun tappingATask_opensItPrefilled_andSavingShowsTheEdit() {
    addTask("Call dentist")

    waitForText("Call dentist")
    composeTestRule.onNodeWithText("Call dentist").performClick()
    composeTestRule.onNodeWithText("Call dentist").performTextReplacement("Call the dentist back")
    composeTestRule.onNodeWithText("Save").performClick()

    waitForText("Call the dentist back")
    composeTestRule.onNodeWithText("Call the dentist back").assertIsDisplayed()
    composeTestRule.onNodeWithText("Call dentist").assertDoesNotExist()
  }

  @Test
  fun deletingATask_asksForConfirmation_thenRemovesIt() {
    addTask("Call dentist")

    waitForText("Call dentist")
    composeTestRule.onNodeWithText("Call dentist").performClick()
    composeTestRule.onNodeWithContentDescription("Delete").performClick()
    waitForText("Delete this reminder?")
    composeTestRule.onNodeWithText("Delete this reminder?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete").performClick()

    waitForText("No reminders yet. Tap + to add one.")
    composeTestRule.onNodeWithText("No reminders yet. Tap + to add one.").assertIsDisplayed()
  }

  /** The Task List updates when Room emits on its own thread, which Compose's test synchronization doesn't wait for. */
  private fun waitForText(text: String) {
    composeTestRule.waitUntil(
      timeoutMillis = 10_000,
      condition = { -> composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() },
    )
  }

  private fun addTask(title: String) {
    composeTestRule.onNodeWithContentDescription("Add task").performClick()
    composeTestRule.onNodeWithText("Title").performTextInput(title)
    composeTestRule.onNodeWithText("Save").performClick()
  }
}
