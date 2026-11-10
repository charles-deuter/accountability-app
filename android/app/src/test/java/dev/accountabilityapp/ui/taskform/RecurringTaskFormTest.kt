package dev.accountabilityapp.ui.taskform

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.MainDispatcherRule
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.Recurrence
import dev.accountabilityapp.data.recurrenceOf
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The Task Form's repeat section. Times are UTC; 2026-10-21 is a Wednesday. */
@RunWith(AndroidJUnit4::class)
class RecurringTaskFormTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val clock = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC)
  private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
  private val taskService = AppContainer(context, clock = clock, database = database).taskService

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun choosingWeekly_startsWithTodaysWeekdayTicked() {
    val viewModel = TaskFormViewModel(taskService, clock)

    viewModel.onRepeatChange(Repeat.WEEKLY)

    assertEquals(setOf(DayOfWeek.WEDNESDAY), viewModel.uiState.value.weekdays)
    assertFalse(viewModel.uiState.value.isPaused)
  }

  @Test
  fun untickingTheLastDay_pausesTheSeries_andTheSwitchStaysLockedOnPaused() {
    val viewModel = TaskFormViewModel(taskService, clock)
    viewModel.onRepeatChange(Repeat.WEEKLY)

    viewModel.onWeekdayToggle(DayOfWeek.WEDNESDAY)
    viewModel.onPausedChange(false)

    assertEquals(emptySet<DayOfWeek>(), viewModel.uiState.value.weekdays)
    assertTrue(viewModel.uiState.value.isPaused)
    assertTrue(isPauseLocked(viewModel.uiState.value))
  }

  @Test
  fun tickingADayAfterAnAutomaticPause_resumesTheSeries() {
    val viewModel = TaskFormViewModel(taskService, clock)
    viewModel.onRepeatChange(Repeat.WEEKLY)
    viewModel.onWeekdayToggle(DayOfWeek.WEDNESDAY)

    viewModel.onWeekdayToggle(DayOfWeek.MONDAY)

    assertFalse(viewModel.uiState.value.isPaused)
    assertFalse(isPauseLocked(viewModel.uiState.value))
  }

  @Test
  fun aSeriesPausedByHand_staysPausedWhenItsDaysChange() {
    val viewModel = TaskFormViewModel(taskService, clock)
    viewModel.onRepeatChange(Repeat.WEEKLY)
    viewModel.onPausedChange(true)

    viewModel.onWeekdayToggle(DayOfWeek.WEDNESDAY)
    viewModel.onWeekdayToggle(DayOfWeek.MONDAY)

    assertTrue(viewModel.uiState.value.isPaused)
  }

  @Test
  fun savingADailyReminder_createsItsFirstInstanceAtTheChosenTimeOfDay() = runTest(testBody = { ->
    val viewModel = TaskFormViewModel(taskService, clock)
    viewModel.onTitleChange("Take meds")
    viewModel.onRepeatChange(Repeat.DAILY)
    viewModel.onScheduledTimeChange(Instant.parse("2026-10-21T09:00:00Z"))

    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    val instance = taskService.observeTaskList().first().single()
    val template = taskService.findTemplate(instance.templateId!!)!!
    assertEquals("Take meds", instance.title)
    assertEquals(Instant.parse("2026-10-22T09:00:00Z"), instance.scheduledTime)
    assertEquals(Recurrence.Daily, recurrenceOf(template))
  })

  @Test
  fun savingAPausedWeeklyReminder_addsNothingToTheTaskList() = runTest(testBody = { ->
    val viewModel = TaskFormViewModel(taskService, clock)
    viewModel.onTitleChange("Stretch")
    viewModel.onRepeatChange(Repeat.WEEKLY)
    viewModel.onWeekdayToggle(DayOfWeek.WEDNESDAY)

    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(emptyList<String>(), taskService.observeTaskList().first().map({ task -> task.title }))
  })

  @Test
  fun openingAnInstance_showsItsSeries_andWhenTheNextOneWouldBe() = runTest(testBody = { ->
    val mondayAndFriday = Recurrence.Weekly(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
    taskService.addRecurring(title = "Gym", recurrence = mondayAndFriday, timeOfDay = LocalTime.of(9, 0), isActive = true)
    val instance = taskService.observeTaskList().first().single()

    val viewModel = TaskFormViewModel(taskService, clock, taskId = instance.id)

    val loaded = viewModel.uiState.first({ state -> !state.isLoading })
    assertEquals(mondayAndFriday, loaded.series?.recurrence)
    assertEquals(LocalTime.of(9, 0), loaded.series?.timeOfDay)
    assertEquals(Instant.parse("2026-10-26T09:00:00Z"), loaded.series?.nextInstanceTime)
  })

  @Test
  fun openingAOneOffTask_showsNoSeries() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))

    val viewModel = TaskFormViewModel(taskService, clock, taskId = taskId)

    assertNull(viewModel.uiState.first({ state -> !state.isLoading }).series)
  })

  @Test
  fun skippingAnInstance_replacesItWithTheNextOne_andFinishes() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = LocalTime.of(9, 0), isActive = true)
    val instance = taskService.observeTaskList().first().single()
    val viewModel = TaskFormViewModel(taskService, clock, taskId = instance.id)
    viewModel.uiState.first({ state -> !state.isLoading })

    viewModel.skip()

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(listOf(Instant.parse("2026-10-23T09:00:00Z")), taskService.observeTaskList().first().map({ task -> task.scheduledTime }))
  })
}
