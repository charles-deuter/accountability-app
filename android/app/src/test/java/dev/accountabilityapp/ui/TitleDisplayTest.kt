package dev.accountabilityapp.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.data.TASK_TITLE_MAX_LENGTH
import dev.accountabilityapp.ui.reminder.ReminderScreen
import dev.accountabilityapp.ui.taskform.TaskFormScreen
import dev.accountabilityapp.ui.taskform.TaskFormUiState
import dev.accountabilityapp.ui.tasklist.TaskListItem
import dev.accountabilityapp.ui.tasklist.TaskListScreen
import dev.accountabilityapp.ui.tasklist.TaskListUiState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Long titles at the screen level: real Compose layout at phone sizes, through Robolectric's native graphics. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TitleDisplayTest {

  @get:Rule val composeRule = createComposeRule()

  /** Words, so the text wraps the way a real title does. */
  private val longestTitle = "Call the dentist about the appointment and then ring the pharmacy back ab".padEnd(TASK_TITLE_MAX_LENGTH, 'x')

  @Test
  fun theTaskList_showsAtMostTwoLinesOfATitle_endingInAnEllipsis() {
    val item = TaskListItem(1, longestTitle, Instant.parse("2026-10-21T09:00:00Z"), isOverdue = false)
    composeRule.setContent { TaskListScreen(state = TaskListUiState(listOf(item)), onAddTask = { -> }, onEditTask = { _ -> }) }

    val layout = textLayoutOf(composeRule.onNodeWithText(longestTitle))

    assertEquals(2, layout.layoutInput.maxLines)
    assertEquals(TextOverflow.Ellipsis, layout.layoutInput.overflow)
    assertTrue(layout.lineCount <= 2)
  }

  @Test
  fun theTakeover_showsAtMostThreeLinesOfATitle_endingInAnEllipsis() {
    composeRule.setContent { ReminderScreen(title = longestTitle, onDone = { -> }, onSnooze = { -> }) }

    val layout = textLayoutOf(composeRule.onNodeWithText(longestTitle))

    assertEquals(3, layout.layoutInput.maxLines)
    assertEquals(TextOverflow.Ellipsis, layout.layoutInput.overflow)
    assertTrue(layout.lineCount <= 3)
  }

  @Test
  @Config(qualifiers = "w411dp-h891dp-port")
  fun doneAndSnooze_stayOnTheTakeover_withTheLongestTitle_inPortrait() {
    composeRule.setContent { ReminderScreen(title = longestTitle, onDone = { -> }, onSnooze = { -> }) }

    assertButtonIsUsable("Done")
    assertButtonIsUsable("Snooze")
  }

  @Test
  @Config(qualifiers = "w800dp-h360dp-land")
  fun doneAndSnooze_stayOnTheTakeover_withTheLongestTitle_inLandscape() {
    composeRule.setContent { ReminderScreen(title = longestTitle, onDone = { -> }, onSnooze = { -> }) }

    assertButtonIsUsable("Done")
    assertButtonIsUsable("Snooze")
  }

  @Test
  fun theTaskForm_countsTheTitleAgainstTheLimit() {
    val state = TaskFormUiState(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T09:00:00Z"))
    composeRule.setContent {
      TaskFormScreen(state = state, onTitleChange = { _ -> }, onScheduledTimeChange = { _ -> }, onSave = { -> }, onDelete = { -> }, onBack = { -> })
    }

    composeRule.onNodeWithText("12/$TASK_TITLE_MAX_LENGTH").assertIsDisplayed()
  }

  /** Fully on the screen and still a real button: a Column out of room squashes its last children to nothing. */
  private fun assertButtonIsUsable(text: String) {
    val button = composeRule.onNodeWithText(text)
    button.assertIsDisplayed().assertHeightIsAtLeast(48.dp)
    val screen = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
    val bounds = button.fetchSemanticsNode().boundsInRoot
    assertTrue("$text ends at ${bounds.bottom}, below the screen's ${screen.bottom}", bounds.bottom <= screen.bottom)
  }

  private fun textLayoutOf(node: SemanticsNodeInteraction): TextLayoutResult {
    val results = mutableListOf<TextLayoutResult>()
    val semantics = node.fetchSemanticsNode().config
    val textLayoutAction = semantics.getOrNull(SemanticsActions.GetTextLayoutResult)?.action
    textLayoutAction?.invoke(results)
    return results.single()
  }
}
