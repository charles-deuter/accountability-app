package dev.accountabilityapp.ui.taskform

import android.app.Notification
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
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class TaskFormViewModelTest {

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
  fun savingABlankTitle_showsTitleRequired_andSavesNothing() = runTest(testBody = { ->
    val viewModel = TaskFormViewModel(taskService, clock)

    viewModel.onTitleChange("   ")
    viewModel.save()

    assertTrue(viewModel.uiState.value.showTitleRequired)
    assertEquals(emptyList<String>(), taskService.observeTaskList().first().map({ task -> task.title }))
  })

  @Test
  fun savingATitleAndTime_addsTheTask_andFinishes() = runTest(testBody = { ->
    val viewModel = TaskFormViewModel(taskService, clock)

    viewModel.onTitleChange("Call dentist")
    viewModel.onScheduledTimeChange(Instant.parse("2026-10-23T15:30:00Z"))
    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    val saved = taskService.observeTaskList().first().single()
    assertEquals("Call dentist", saved.title)
    assertEquals(Instant.parse("2026-10-23T15:30:00Z"), saved.scheduledTime)
  })

  @Test
  fun editingATask_startsPrefilledWithItsTitleAndTime() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-23T15:30:00Z"))

    val viewModel = TaskFormViewModel(taskService, clock, taskId = taskId)

    val form = viewModel.uiState.first({ state -> !state.isLoading })
    assertEquals("Call dentist", form.title)
    assertEquals(Instant.parse("2026-10-23T15:30:00Z"), form.scheduledTime)
  })

  @Test
  fun savingAnEdit_updatesTheTask_insteadOfAddingOne() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-23T15:30:00Z"))
    val viewModel = TaskFormViewModel(taskService, clock, taskId = taskId)
    viewModel.uiState.first({ state -> !state.isLoading })

    viewModel.onTitleChange("Call the dentist back")
    viewModel.onScheduledTimeChange(Instant.parse("2026-10-24T10:00:00Z"))
    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    val saved = taskService.observeTaskList().first().single()
    assertEquals(taskId, saved.id)
    assertEquals("Call the dentist back", saved.title)
    assertEquals(Instant.parse("2026-10-24T10:00:00Z"), saved.scheduledTime)
  })

  @Test
  fun editingASnoozedTask_showsItsFireTime() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    taskService.snooze(taskId, SnoozeDuration.ONE_HOUR)

    val viewModel = TaskFormViewModel(taskService, clock, taskId = taskId)

    val form = viewModel.uiState.first({ state -> !state.isLoading })
    assertEquals(Instant.parse("2026-10-21T13:00:00Z"), form.scheduledTime)
  })

  @Test
  fun savingANewTitleOnASnoozedTask_keepsTheSnooze() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Cal dentist", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    taskService.snooze(taskId, SnoozeDuration.ONE_HOUR)
    val viewModel = TaskFormViewModel(taskService, clock, taskId = taskId)
    viewModel.uiState.first({ state -> !state.isLoading })

    viewModel.onTitleChange("Call dentist")
    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    val saved = taskService.findTask(taskId)!!
    assertEquals("Call dentist", saved.title)
    assertEquals(TaskStatus.SNOOZED, saved.status)
    assertEquals(Instant.parse("2026-10-21T13:00:00Z"), saved.snoozedUntil)
    assertEquals(1, saved.snoozeCount)
    // The original time has passed, so ending the Snooze here would ring the Reminder straight away.
    val notificationManager = shadowOf(context.getSystemService(NotificationManager::class.java))
    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
  })

  @Test
  fun savingANewTimeOnASnoozedTask_endsTheSnooze() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    taskService.snooze(taskId, SnoozeDuration.ONE_HOUR)
    val viewModel = TaskFormViewModel(taskService, clock, taskId = taskId)
    viewModel.uiState.first({ state -> !state.isLoading })

    viewModel.onScheduledTimeChange(Instant.parse("2026-10-21T18:00:00Z"))
    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    val saved = taskService.findTask(taskId)!!
    assertEquals(TaskStatus.PENDING, saved.status)
    assertEquals(Instant.parse("2026-10-21T18:00:00Z"), saved.scheduledTime)
    assertEquals(1, saved.snoozeCount)
  })

  @Test
  fun deletingATask_removesIt_andFinishes() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-23T15:30:00Z"))
    val viewModel = TaskFormViewModel(taskService, clock, taskId = taskId)
    viewModel.uiState.first({ state -> !state.isLoading })

    viewModel.delete()

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(emptyList<String>(), taskService.observeTaskList().first().map({ task -> task.title }))
  })
}
