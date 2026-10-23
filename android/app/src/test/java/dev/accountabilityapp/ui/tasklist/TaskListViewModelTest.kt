package dev.accountabilityapp.ui.tasklist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.MainDispatcherRule
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.TaskRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
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
  private val repository = TaskRepository(database.taskDao(), noon)

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun aTaskWhoseFireTimeHasPassed_isOverdue_andAFutureTaskIsNot() = runTest {
    repository.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    repository.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T13:00:00Z"))
    val viewModel = TaskListViewModel(repository, noon)

    val items = viewModel.uiState.first { state -> state.items.size == 2 }.items

    assertEquals(mapOf("Take meds" to true, "Call dentist" to false), items.associate { item -> item.title to item.isOverdue })
  }

  @Test
  fun whileTheListIsObserved_aTaskTurnsOverdueWithinAMinuteOfItsFireTime() = runTest {
    val clock = SettableClock(Instant.parse("2026-10-21T12:00:00Z"))
    repository.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T12:00:30Z"))
    val viewModel = TaskListViewModel(repository, clock)
    backgroundScope.launch { viewModel.uiState.collect { _ -> } }
    val before = viewModel.uiState.first { state -> state.items.isNotEmpty() }.items.single()

    clock.now = Instant.parse("2026-10-21T12:01:00Z")
    advanceTimeBy(60_000)
    runCurrent()

    assertEquals(false, before.isOverdue)
    assertEquals(true, viewModel.uiState.value.items.single().isOverdue)
  }
}

/** A wall clock the test moves by hand, independent of the coroutine scheduler's virtual time. */
private class SettableClock(var now: Instant) : Clock() {
  override fun instant(): Instant = now

  override fun getZone(): ZoneId = ZoneOffset.UTC

  override fun withZone(zone: ZoneId?): Clock = this
}
