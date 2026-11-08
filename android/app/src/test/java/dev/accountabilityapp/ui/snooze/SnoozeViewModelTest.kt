package dev.accountabilityapp.ui.snooze

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.MainDispatcherRule
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.SnoozeDuration
import dev.accountabilityapp.data.TaskStatus
import dev.accountabilityapp.reminders.CHANNEL_ID
import dev.accountabilityapp.reminders.SILENCED_CHANNEL_ID
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
import org.robolectric.Shadows.shadowOf

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
  fun openingThePicker_stopsTheRinging_beforeAnythingIsPicked() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    val notificationManager = shadowOf(context.getSystemService(NotificationManager::class.java))
    assertEquals(listOf(CHANNEL_ID), notificationManager.allNotifications.map({ reminder -> reminder.channelId }))

    SnoozeViewModel(taskService, medsId)
    testScheduler.advanceUntilIdle()

    assertEquals(listOf(SILENCED_CHANNEL_ID), notificationManager.allNotifications.map({ reminder -> reminder.channelId }))
    assertEquals(TaskStatus.PENDING, taskService.findTask(medsId)?.status)
  })

  @Test
  fun pickingAPreset_snoozesForThatLong_andFinishes() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    val viewModel = SnoozeViewModel(taskService, medsId)

    viewModel.snooze(SnoozeDuration.TWO_HOURS)

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(Instant.parse("2026-10-24T15:00:00Z"), taskService.findTask(medsId)?.scheduledTime)
  })

  @Test
  fun leavingWithoutPicking_snoozesForFifteenMinutes() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    val viewModel = SnoozeViewModel(taskService, medsId)

    viewModel.leaveWithoutPicking()

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(Instant.parse("2026-10-24T13:15:00Z"), taskService.findTask(medsId)?.scheduledTime)
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
    assertEquals(Instant.parse("2026-10-24T14:00:00Z"), snoozed.scheduledTime)
  })
}
