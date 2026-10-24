package dev.accountabilityapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val clock = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC)
  private lateinit var database: AppDatabase
  private lateinit var repository: TaskRepository

  @Before
  fun setUp() {
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    repository = TaskRepository(database.taskDao(), clock)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun addedTask_appearsInTheTaskList() = runTest(testBody = { ->
    repository.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))

    val taskList = repository.observeTaskList().first()

    assertEquals(listOf("Call dentist"), taskList.map({ task -> task.title }))
    assertEquals(Instant.parse("2026-10-22T09:00:00Z"), taskList.single().scheduledTime)
  })

  @Test
  fun taskList_isOrderedByFireTime_notByWhenTasksWereAdded() = runTest(testBody = { ->
    repository.add(title = "Pay rent", scheduledTime = Instant.parse("2026-11-01T10:00:00Z"))
    repository.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T09:00:00Z"))
    repository.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T15:00:00Z"))

    val titles = repository.observeTaskList().first().map({ task -> task.title })

    assertEquals(listOf("Take meds", "Call dentist", "Pay rent"), titles)
  })

  @Test
  fun completedTask_leavesTheTaskList() = runTest(testBody = { ->
    val dentistId = repository.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))
    repository.add(title = "Pay rent", scheduledTime = Instant.parse("2026-11-01T10:00:00Z"))

    repository.complete(dentistId)

    assertEquals(listOf("Pay rent"), repository.observeTaskList().first().map({ task -> task.title }))
  })

  @Test
  fun tasks_surviveClosingAndReopeningTheAppDatabase() = runTest(testBody = { ->
    val fileName = "restart-test.db"
    context.deleteDatabase(fileName)
    val firstLaunch = AppDatabase.open(context, fileName)
    TaskRepository(firstLaunch.taskDao(), clock).add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))
    firstLaunch.close()

    val secondLaunch = AppDatabase.open(context, fileName)
    val titles = TaskRepository(secondLaunch.taskDao(), clock).observeTaskList().first().map({ task -> task.title })
    secondLaunch.close()

    assertEquals(listOf("Call dentist"), titles)
  })
}
