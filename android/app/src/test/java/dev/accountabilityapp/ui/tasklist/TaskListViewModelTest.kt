package dev.accountabilityapp.ui.tasklist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.MainDispatcherRule
import dev.accountabilityapp.SettableClock
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.Recurrence
import dev.accountabilityapp.data.SnoozeDuration
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// advanceTimeBy and runCurrent drive the one-minute tick on virtual time; both are marked experimental.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TaskListViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val noon = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC)
  private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
  private val taskService = AppContainer(context, clock = noon, database = database).taskService

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun aTaskWhoseFireTimeHasPassed_isOverdue_andAFutureTaskIsNot() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T13:00:00Z"))
    val viewModel = TaskListViewModel(taskService, noon)

    val items = viewModel.uiState.first({ state -> state.items.size == 2 }).items

    assertEquals(mapOf("Take meds" to true, "Call dentist" to false), items.associate({ item -> item.title to item.isOverdue }))
  })

  @Test
  fun anInstance_isMarkedRecurring_andAOneOffTaskIsNot() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = LocalTime.of(9, 0), isActive = true)
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T13:00:00Z"))
    val viewModel = TaskListViewModel(taskService, noon)

    val items = viewModel.uiState.first({ state -> state.items.size == 2 }).items

    assertEquals(mapOf("Take meds" to true, "Call dentist" to false), items.associate({ item -> item.title to item.isRecurring }))
  })

  @Test
  fun aSnoozedTask_carriesItsSnoozeCountAndSnoozedUntilTime_forTheBadge() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    taskService.snooze(medsId, SnoozeDuration.FIFTEEN_MINUTES)
    taskService.snooze(medsId, SnoozeDuration.THIRTY_MINUTES)
    val viewModel = TaskListViewModel(taskService, noon)

    val item = viewModel.uiState.first({ state -> state.items.isNotEmpty() }).items.single()

    assertEquals(2, item.snoozeCount)
    assertEquals(Instant.parse("2026-10-21T12:30:00Z"), item.snoozedUntil)
  })

  @Test
  fun whileTheListIsObserved_aTaskTurnsOverdueWithinAMinuteOfItsFireTime() = runTest(testBody = { ->
    val clock = SettableClock(Instant.parse("2026-10-21T12:00:30Z"))
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T12:01:00Z"))
    val viewModel = TaskListViewModel(taskService, clock)
    backgroundScope.launch(block = { -> viewModel.uiState.collect({ _ -> }) })
    val before = viewModel.uiState.first({ state -> state.items.isNotEmpty() }).items.single()

    clock.now = Instant.parse("2026-10-21T12:01:30Z")
    advanceTimeBy(60_000)
    runCurrent()

    assertEquals(false, before.isOverdue)
    assertEquals(true, viewModel.uiState.value.items.single().isOverdue)
  })
}
