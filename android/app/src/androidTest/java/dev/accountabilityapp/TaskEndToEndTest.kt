package dev.accountabilityapp

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
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

    waitFor(taskListRowOf("Call dentist"))
    composeTestRule.onNode(taskListRowOf("Call dentist")).assertIsDisplayed()
  }

  @Test
  fun addingTwoTasksInARow_showsBoth() {
    addTask("Call dentist")
    addTask("Pay rent")

    composeTestRule.onNode(taskListRowOf("Call dentist")).assertIsDisplayed()
    composeTestRule.onNode(taskListRowOf("Pay rent")).assertIsDisplayed()
  }

  @Test
  fun tappingATask_opensItPrefilled_andSavingShowsTheEdit() {
    addTask("Call dentist")

    openTask("Call dentist")
    composeTestRule.onNode(titleFieldOf("Call dentist")).performTextReplacement("Call the dentist back")
    composeTestRule.onNodeWithText("Save").performClick()

    waitFor(taskListRowOf("Call the dentist back"))
    composeTestRule.onNode(taskListRowOf("Call the dentist back")).assertIsDisplayed()
    composeTestRule.onNodeWithText("Call dentist").assertDoesNotExist()
  }

  @Test
  fun deletingATask_asksForConfirmation_thenRemovesIt() {
    addTask("Call dentist")

    openTask("Call dentist")
    composeTestRule.onNodeWithContentDescription("Delete").performClick()
    waitFor(hasText("Delete this reminder?"))
    composeTestRule.onNodeWithText("Delete this reminder?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete").performClick()

    waitFor(hasText("No reminders yet. Tap + to add one."))
    composeTestRule.onNodeWithText("No reminders yet. Tap + to add one.").assertIsDisplayed()
  }

  /**
   * Screens fill in when Room answers on its own thread, which Compose's test synchronization doesn't wait for. So a
   * step waits for what it needs on screen rather than for Compose to be idle.
   */
  private fun waitFor(matcher: SemanticsMatcher) {
    composeTestRule.waitUntil(
      timeoutMillis = 10_000,
      condition = { -> composeTestRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() },
    )
  }

  /** Adds a Task and waits until the Task List shows it, so the next step starts from the list. */
  private fun addTask(title: String) {
    composeTestRule.onNodeWithContentDescription("Add task").performClick()
    composeTestRule.onNodeWithText("Title").performTextInput(title)
    composeTestRule.onNodeWithText("Save").performClick()
    waitFor(taskListRowOf(title))
  }

  /** Taps a Task in the list and waits until the Task Form has loaded its title. */
  private fun openTask(title: String) {
    composeTestRule.onNode(taskListRowOf(title)).performClick()
    waitFor(titleFieldOf(title))
  }
}

/** A Task List row: text you can't type into, unlike the Task Form's title field showing the same title. */
private fun taskListRowOf(title: String): SemanticsMatcher {
  val isEditable = hasSetTextAction()
  return hasText(title) and !isEditable
}

/** The Task Form's title field once it holds [title]. */
private fun titleFieldOf(title: String): SemanticsMatcher = hasText(title) and hasSetTextAction()
