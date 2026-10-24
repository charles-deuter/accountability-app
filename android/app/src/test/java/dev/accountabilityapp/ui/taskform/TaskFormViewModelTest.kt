package dev.accountabilityapp.ui.taskform

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.MainDispatcherRule
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.TaskRepository
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

@RunWith(AndroidJUnit4::class)
class TaskFormViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val clock = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC)
  private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
  private val repository = TaskRepository(database.taskDao(), clock)

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun savingABlankTitle_showsTitleRequired_andSavesNothing() = runTest(testBody = { ->
    val viewModel = TaskFormViewModel(repository, clock)

    viewModel.onTitleChange("   ")
    viewModel.save()

    assertTrue(viewModel.uiState.value.showTitleRequired)
    assertEquals(emptyList<String>(), repository.observeTaskList().first().map({ task -> task.title }))
  })

  @Test
  fun savingATitleAndTime_addsTheTask_andFinishes() = runTest(testBody = { ->
    val viewModel = TaskFormViewModel(repository, clock)

    viewModel.onTitleChange("Call dentist")
    viewModel.onScheduledTimeChange(Instant.parse("2026-10-23T15:30:00Z"))
    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    val saved = repository.observeTaskList().first().single()
    assertEquals("Call dentist", saved.title)
    assertEquals(Instant.parse("2026-10-23T15:30:00Z"), saved.scheduledTime)
  })

  @Test
  fun editingATask_startsPrefilledWithItsTitleAndTime() = runTest(testBody = { ->
    val taskId = repository.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-23T15:30:00Z"))

    val viewModel = TaskFormViewModel(repository, clock, taskId = taskId)

    val form = viewModel.uiState.first({ state -> !state.isLoading })
    assertEquals("Call dentist", form.title)
    assertEquals(Instant.parse("2026-10-23T15:30:00Z"), form.scheduledTime)
  })

  @Test
  fun savingAnEdit_updatesTheTask_insteadOfAddingOne() = runTest(testBody = { ->
    val taskId = repository.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-23T15:30:00Z"))
    val viewModel = TaskFormViewModel(repository, clock, taskId = taskId)
    viewModel.uiState.first({ state -> !state.isLoading })

    viewModel.onTitleChange("Call the dentist back")
    viewModel.onScheduledTimeChange(Instant.parse("2026-10-24T10:00:00Z"))
    viewModel.save()

    viewModel.uiState.first({ state -> state.isFinished })
    val saved = repository.observeTaskList().first().single()
    assertEquals(taskId, saved.id)
    assertEquals("Call the dentist back", saved.title)
    assertEquals(Instant.parse("2026-10-24T10:00:00Z"), saved.scheduledTime)
  })

  @Test
  fun deletingATask_removesIt_andFinishes() = runTest(testBody = { ->
    val taskId = repository.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-23T15:30:00Z"))
    val viewModel = TaskFormViewModel(repository, clock, taskId = taskId)
    viewModel.uiState.first({ state -> !state.isLoading })

    viewModel.delete()

    viewModel.uiState.first({ state -> state.isFinished })
    assertEquals(emptyList<String>(), repository.observeTaskList().first().map({ task -> task.title }))
  })
}
