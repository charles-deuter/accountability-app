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
import org.junit.Assert.assertNull
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
  fun aSnoozedTask_isOrderedByItsSnoozedUntilTime() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-21T12:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T12:30:00Z"))

    taskService.snooze(medsId, SnoozeDuration.ONE_HOUR)

    val titles = taskService.observeTaskList().first().map({ task -> task.title })
    assertEquals(listOf("Call dentist", "Take meds"), titles)
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
  fun changingOnlyTheTitleOfASnoozedTask_keepsItsSnooze() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Cal dentist", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    taskService.snooze(taskId, SnoozeDuration.ONE_HOUR)

    taskService.update(taskId, title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T13:00:00Z"))

    val edited = taskService.findTask(taskId)!!
    assertEquals("Call dentist", edited.title)
    assertEquals(TaskStatus.SNOOZED, edited.status)
    assertEquals(Instant.parse("2026-10-21T13:00:00Z"), edited.snoozedUntil)
    assertEquals(1, edited.snoozeCount)
    assertEquals(Instant.parse("2026-10-21T11:00:00Z"), edited.scheduledTime)
  })

  @Test
  fun confirmingTheTimePickerWithoutChangingIt_keepsTheSnooze_thoughThePickerDropsTheSeconds() = runTest(testBody = { ->
    val snoozedUntil = Instant.parse("2026-10-21T13:00:12.345Z")
    val taskId =
      database.taskDao().insert(
        Task(
          title = "Call dentist",
          scheduledTime = Instant.parse("2026-10-21T11:00:00Z"),
          status = TaskStatus.SNOOZED,
          snoozeCount = 1,
          snoozedUntil = snoozedUntil,
          createdAt = Instant.parse("2026-10-21T10:00:00Z"),
        )
      )

    taskService.update(taskId, title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T13:00:00Z"))

    val edited = taskService.findTask(taskId)!!
    assertEquals(TaskStatus.SNOOZED, edited.status)
    assertEquals(snoozedUntil, edited.snoozedUntil)
  })

  @Test
  fun changingTheTimeOfASnoozedTask_endsTheSnooze_andTheSnoozeCountStaysAsHistory() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T11:00:00Z"))
    taskService.snooze(taskId, SnoozeDuration.ONE_HOUR)

    taskService.update(taskId, title = "Call dentist", scheduledTime = Instant.parse("2026-10-21T18:00:00Z"))

    val edited = taskService.findTask(taskId)!!
    assertEquals(TaskStatus.PENDING, edited.status)
    assertNull(edited.snoozedUntil)
    assertEquals(1, edited.snoozeCount)
    assertEquals(Instant.parse("2026-10-21T18:00:00Z"), edited.scheduledTime)
  })

  @Test
  fun changingOnlyTheTitleOfAPendingTask_leavesItPendingAtItsTime() = runTest(testBody = { ->
    val taskId = taskService.add(title = "Cal dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))

    taskService.update(taskId, title = "Call dentist", scheduledTime = Instant.parse("2026-10-22T09:00:00Z"))

    val edited = taskService.findTask(taskId)!!
    assertEquals("Call dentist", edited.title)
    assertEquals(TaskStatus.PENDING, edited.status)
    assertEquals(Instant.parse("2026-10-22T09:00:00Z"), edited.scheduledTime)
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
