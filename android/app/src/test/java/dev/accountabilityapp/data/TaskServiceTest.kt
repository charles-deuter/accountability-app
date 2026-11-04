package dev.accountabilityapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
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
class TaskServiceTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val clock = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC)
  private lateinit var database: AppDatabase
  private lateinit var taskService: TaskService

  @Before
  fun setUp() {
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    taskService = AppContainer(context, clock = clock, database = database).taskService
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun addedTask_appearsInTheTaskList() = runTest(testBody = { ->
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))

    val taskList = taskService.observeTaskList().first()

    assertEquals(listOf("Call dentist"), taskList.map({ task -> task.title }))
    assertEquals(Instant.parse("2026-10-22T09:00:00Z"), taskList.single().scheduledTime)
  })

  @Test
  fun taskList_isOrderedByFireTime_notByWhenTasksWereAdded() = runTest(testBody = { ->
    taskService.add(title = "Pay rent", scheduledTime = Instant.parse("2026-11-01T10:00:00Z"))
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T09:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T15:00:00Z"))

    val titles = taskService.observeTaskList().first().map({ task -> task.title })

    assertEquals(listOf("Take meds", "Call dentist", "Pay rent"), titles)
  })

  @Test
  fun completedTask_leavesTheTaskList() = runTest(testBody = { ->
    val dentistId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))
    taskService.add(title = "Pay rent", scheduledTime = Instant.parse("2026-11-01T10:00:00Z"))

    taskService.complete(dentistId)

    assertEquals(listOf("Pay rent"), taskService.observeTaskList().first().map({ task -> task.title }))
  })

  @Test
  fun editedTask_changesInPlace_ratherThanAddingANewOne() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))

    taskService.update(taskId, title = "Call the dentist back", scheduledTime = Instant.parse("2026-10-23T14:00:00Z"))

    val edited = taskService.observeTaskList().first().single()
    assertEquals(taskId, edited.id)
    assertEquals("Call the dentist back", edited.title)
    assertEquals(Instant.parse("2026-10-23T14:00:00Z"), edited.scheduledTime)
  })

  @Test
  fun deletedTask_leavesTheTaskList() = runTest(testBody = { ->
    val dentistId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))
    taskService.add(title = "Pay rent", scheduledTime = Instant.parse("2026-11-01T10:00:00Z"))

    taskService.delete(dentistId)

    assertEquals(listOf("Pay rent"), taskService.observeTaskList().first().map({ task -> task.title }))
  })

  @Test
  fun tasks_surviveClosingAndReopeningTheAppDatabase() = runTest(testBody = { ->
    val fileName = "restart-test.db"
    context.deleteDatabase(fileName)
    val firstLaunch = AppDatabase.open(context, fileName)
    AppContainer(context, clock = clock, database = firstLaunch).taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))
    firstLaunch.close()

    val secondLaunch = AppDatabase.open(context, fileName)
    val titles = AppContainer(context, clock = clock, database = secondLaunch).taskService.observeTaskList().first().map({ task -> task.title })
    secondLaunch.close()

    assertEquals(listOf("Call dentist"), titles)
  })
}
