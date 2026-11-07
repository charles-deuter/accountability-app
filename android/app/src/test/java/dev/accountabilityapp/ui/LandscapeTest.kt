package dev.accountabilityapp.ui

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.data.SnoozeDuration
import dev.accountabilityapp.ui.snooze.SnoozeScreen
import dev.accountabilityapp.ui.taskform.TaskFormScreen
import dev.accountabilityapp.ui.taskform.TaskFormUiState
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Every screen sideways, on the smallest common phone. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w800dp-h360dp-land")
class LandscapeTest {

  @get:Rule val composeRule = createComposeRule()

  private val scheduledTime = Instant.parse("2026-11-30T20:00:00Z")

  @Test
  fun everySnoozePreset_canBeReached() {
    var picked: SnoozeDuration? = null
    composeRule.setContent { SnoozeScreen(onSnooze = { duration -> picked = duration }) }

    val labels =
      mapOf(
        "15 minutes" to SnoozeDuration.FIFTEEN_MINUTES,
        "30 minutes" to SnoozeDuration.THIRTY_MINUTES,
        "1 hour" to SnoozeDuration.ONE_HOUR,
        "2 hours" to SnoozeDuration.TWO_HOURS,
        "1 day" to SnoozeDuration.ONE_DAY,
      )
    for ((label, duration) in labels) {
      val preset = composeRule.onNodeWithText(label)
      preset.performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(40.dp).performClick()
      assertEquals(duration, picked)
    }
  }

  @Test
  fun theDatePicker_reachesOkCancelAndTheLastDayOfTheMonth() {
    showTaskForm()

    composeRule.onNodeWithText("Nov 30, 2026").performClick()

    assertDialogButtonIsUsable("OK")
    assertDialogButtonIsUsable("Cancel")
    composeRule.onNodeWithText("Monday, November 30, 2026", substring = true).performScrollTo().assertIsDisplayed()
  }

  @Test
  fun theDatePicker_stillOpensItsYearList() {
    showTaskForm()
    composeRule.onNodeWithText("Nov 30, 2026").performClick()

    composeRule.onNodeWithText("November 2026").performClick()

    composeRule.onNodeWithText("2030", substring = true).assertExists()
  }

  @Test
  fun theTimePicker_fitsWithOkAndCancel() {
    showTaskForm()

    composeRule.onNodeWithText(formatTime(scheduledTime, ZoneOffset.UTC)).performClick()

    assertDialogButtonIsUsable("OK")
    assertDialogButtonIsUsable("Cancel")
    // The dial has no text fields. Typing the time is what makes it fit.
    composeRule.onNode(hasSetTextAction() and hasText("08")).assertIsDisplayed()
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp-land")
  fun aSidewaysTablet_keepsTheClockDial() {
    showTaskForm()

    composeRule.onNodeWithText(formatTime(scheduledTime, ZoneOffset.UTC)).performClick()

    composeRule.onNodeWithText("OK").assertIsDisplayed()
    composeRule.onNode(hasSetTextAction() and hasText("08")).assertDoesNotExist()
  }

  @Test
  fun theFormsSaveButton_canBeReached() {
    showTaskForm()

    composeRule.onNodeWithText("Save").performScrollTo().assertIsDisplayed()
  }

  private fun showTaskForm() {
    val state = TaskFormUiState(title = "Call dentist", scheduledTime = scheduledTime)
    composeRule.setContent {
      TaskFormScreen(
        state = state,
        onTitleChange = { _ -> },
        onScheduledTimeChange = { _ -> },
        onSave = { -> },
        onDelete = { -> },
        onBack = { -> },
        zone = ZoneOffset.UTC,
      )
    }
  }

  private fun assertDialogButtonIsUsable(text: String) {
    val button = composeRule.onNodeWithText(text)
    button.assertIsDisplayed().assertHeightIsAtLeast(40.dp)
    val bounds = button.getBoundsInRoot()
    assertTrue("$text ends at ${bounds.bottom}, past the 360dp screen", bounds.bottom <= 360.dp)
  }
}
