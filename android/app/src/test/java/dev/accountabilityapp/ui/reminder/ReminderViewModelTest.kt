package dev.accountabilityapp.ui.reminder

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.MainDispatcherRule
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.TaskStatus
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val clock = Clock.fixed(Instant.parse("2026-10-24T13:00:00Z"), ZoneOffset.UTC)
  private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
  private val taskService = AppContainer(context, clock = clock, database = database).taskService

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun theTakeover_showsTheTasksTitle() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))

    val viewModel = ReminderViewModel(taskService, medsId)

    assertEquals("Take meds", viewModel.uiState.first({ state -> state.title.isNotEmpty() }).title)
  })

  @Test
  fun withNoResponse_theTakeoverClosesAtTheRingLimit_notBefore() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))

    val viewModel = ReminderViewModel(taskService, medsId)

    mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(4.minutes + 59.seconds)
    assertFalse(viewModel.uiState.value.isFinished)
    mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(2.seconds)
    assertTrue(viewModel.uiState.value.isFinished)
  })

  @Test
  fun done_completesTheTask_andClosesTheTakeover() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    val viewModel = ReminderViewModel(taskService, medsId)

    viewModel.complete()

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(TaskStatus.COMPLETED, taskService.findTask(medsId)?.status)
  })
}
