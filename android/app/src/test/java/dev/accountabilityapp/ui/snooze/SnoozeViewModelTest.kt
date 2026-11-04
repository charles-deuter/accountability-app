package dev.accountabilityapp.ui.snooze

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.MainDispatcherRule
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.SnoozeDuration
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnoozeViewModelTest {

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
  fun pickingAPreset_snoozesForThatLong_andFinishes() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    val viewModel = SnoozeViewModel(taskService, medsId)

    viewModel.snooze(SnoozeDuration.TWO_HOURS)

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(Instant.parse("2026-10-24T15:00:00Z"), taskService.findTask(medsId)?.snoozedUntil)
  })

  @Test
  fun leavingWithoutPicking_snoozesForFifteenMinutes() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    val viewModel = SnoozeViewModel(taskService, medsId)

    viewModel.leaveWithoutPicking()

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(Instant.parse("2026-10-24T13:15:00Z"), taskService.findTask(medsId)?.snoozedUntil)
  })

  @Test
  fun leavingAfterPicking_doesNotSnoozeASecondTime() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    val viewModel = SnoozeViewModel(taskService, medsId)

    viewModel.snooze(SnoozeDuration.ONE_HOUR)
    viewModel.uiState.first({ state -> state.isFinished })
    viewModel.leaveWithoutPicking()

    val snoozed = taskService.findTask(medsId)!!
    assertEquals(1, snoozed.snoozeCount)
    assertEquals(Instant.parse("2026-10-24T14:00:00Z"), snoozed.snoozedUntil)
  })
}
