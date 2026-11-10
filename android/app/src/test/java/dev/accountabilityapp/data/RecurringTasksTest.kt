package dev.accountabilityapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.SettableClock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** Templates and their Instances, through TaskService on a real in-memory database. Times are UTC; 2026-10-21 is a Wednesday. */
@RunWith(AndroidJUnit4::class)
class RecurringTasksTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val clock = SettableClock(Instant.parse("2026-10-21T12:00:00Z"))
  private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
  private val taskService = AppContainer(context, clock = clock, database = database).taskService
  private val nineAm = LocalTime.of(9, 0)

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun savingADailyReminder_createsItsTemplate_andOnePendingInstanceAtTheFirstOccurrence() = runTest(testBody = { ->
    val templateId = taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)

    val template = taskService.findTemplate(templateId)!!
    val instance = taskService.observeTaskList().first().single()
    assertEquals("Take meds", template.title)
    assertEquals(Recurrence.Daily, recurrenceOf(template))
    assertEquals(nineAm, template.timeOfDay)
    assertEquals(templateId, instance.templateId)
    assertEquals("Take meds", instance.title)
    assertEquals(Instant.parse("2026-10-22T09:00:00Z"), instance.scheduledTime)
  })

  @Test
  fun savingAPausedReminder_createsItsTemplate_andNoInstance() = runTest(testBody = { ->
    val noDays = Recurrence.Weekly(emptySet())

    val templateId = taskService.addRecurring(title = "Stretch", recurrence = noDays, timeOfDay = nineAm, isActive = false)

    val template = taskService.findTemplate(templateId)!!
    assertEquals(false, template.isActive)
    assertEquals(noDays, recurrenceOf(template))
    assertEquals(emptyList<Task>(), taskService.observeTaskList().first())
  })

  @Test
  fun doneOnAnInstance_completesIt_andCreatesTheNextOne() = runTest(testBody = { ->
    val templateId = taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()
    clock.now = Instant.parse("2026-10-22T09:05:00Z")

    taskService.complete(first.id)

    val next = taskService.observeTaskList().first().single()
    assertEquals(TaskStatus.COMPLETED, taskService.findTask(first.id)!!.status)
    assertEquals(templateId, next.templateId)
    assertEquals("Take meds", next.title)
    assertEquals(Instant.parse("2026-10-23T09:00:00Z"), next.scheduledTime)
  })

  @Test
  fun doneAfterASnooze_continuesFromTheSeriesSlot_notTheSnooze() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()
    clock.now = Instant.parse("2026-10-22T09:00:00Z")
    taskService.snooze(first.id, SnoozeDuration.ONE_HOUR)
    clock.now = Instant.parse("2026-10-22T10:05:00Z")

    taskService.complete(first.id)

    assertEquals(Instant.parse("2026-10-23T09:00:00Z"), taskService.observeTaskList().first().single().scheduledTime)
  })

  @Test
  fun doneEarly_continuesAfterTheSeriesSlot_notAgainTheSameDay() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()
    clock.now = Instant.parse("2026-10-22T08:00:00Z")

    taskService.complete(first.id)

    assertEquals(Instant.parse("2026-10-23T09:00:00Z"), taskService.observeTaskList().first().single().scheduledTime)
  })

  @Test
  fun doneDaysLate_skipsTheMissedOccurrences() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()
    clock.now = Instant.parse("2026-10-25T12:00:00Z")

    taskService.complete(first.id)

    assertEquals(Instant.parse("2026-10-26T09:00:00Z"), taskService.observeTaskList().first().single().scheduledTime)
  })

  @Test
  fun anInstanceMovedEarlier_stillContinuesFromItsSeriesSlot() = runTest(testBody = { ->
    val mondays = Recurrence.Weekly(setOf(DayOfWeek.MONDAY))
    taskService.addRecurring(title = "Take out bins", recurrence = mondays, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()
    taskService.reschedule(first.id, title = "Take out bins", scheduledTime = Instant.parse("2026-10-25T18:00:00Z"))
    clock.now = Instant.parse("2026-10-25T18:05:00Z")

    taskService.complete(first.id)

    assertEquals(Instant.parse("2026-11-02T09:00:00Z"), taskService.observeTaskList().first().single().scheduledTime)
  })

  @Test
  fun doneTwiceOnTheSameInstance_createsOnlyOneNextInstance() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()

    taskService.complete(first.id)
    taskService.complete(first.id)

    assertEquals(listOf(Instant.parse("2026-10-23T09:00:00Z")), taskService.observeTaskList().first().map({ task -> task.scheduledTime }))
  })

  @Test
  fun skippingAnInstance_removesIt_andCreatesTheNextOne() = runTest(testBody = { ->
    val templateId = taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()

    taskService.skip(first.id)

    val next = taskService.observeTaskList().first().single()
    assertNull(taskService.findTask(first.id))
    assertEquals(templateId, next.templateId)
    assertEquals(Instant.parse("2026-10-23T09:00:00Z"), next.scheduledTime)
  })

  @Test
  fun skippingAnInstanceThatWasAlreadyDone_changesNothing() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = nineAm, isActive = true)
    val first = taskService.observeTaskList().first().single()
    taskService.complete(first.id)

    taskService.skip(first.id)

    assertEquals(TaskStatus.COMPLETED, taskService.findTask(first.id)?.status)
    assertEquals(listOf(Instant.parse("2026-10-23T09:00:00Z")), taskService.observeTaskList().first().map({ task -> task.scheduledTime }))
  })
}
