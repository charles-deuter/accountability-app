package dev.accountabilityapp.data

import androidx.room.withTransaction
import dev.accountabilityapp.reminders.ReminderSync
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow

/**
 * Task operations. Each write is followed by a [ReminderSync], so the alarm and notifications follow the database. Every
 * time a Task is given is rounded up to a whole minute.
 */
class TaskService(private val database: AppDatabase, private val clock: Clock, private val reminderSync: ReminderSync) {

  private val taskDao = database.taskDao()
  private val templateDao = database.templateDao()


  /** Tasks shown in the Task List. */
  fun observeTaskList(): Flow<List<Task>> = taskDao.observeTaskList()

  suspend fun findTask(taskId: Long): Task? = taskDao.findTask(taskId)

  suspend fun findTemplate(templateId: Long): Template? = templateDao.findTemplate(templateId)

  /** Saves a recurring reminder. Returns the new Template's id. */
  suspend fun addRecurring(title: String, recurrence: Recurrence, timeOfDay: LocalTime, isActive: Boolean): Long {
    val now = clock.instant()
    val wholeMinute = timeOfDay.truncatedTo(ChronoUnit.MINUTES)
    val template = templateOf(title, recurrence, wholeMinute, isActive, createdAt = now)
    val firstInstanceTime = firstInstanceTimeOf(template, now, clock.zone)
    val templateId = database.withTransaction(block = { ->
      val insertedId = templateDao.insert(template)
      if (firstInstanceTime != null) insertInstance(template.copy(id = insertedId), firstInstanceTime)
      insertedId
    })
    reminderSync.sync()
    return templateId
  }

  suspend fun add(title: String, scheduledTime: Instant): Long {
    val task = Task(title = title, scheduledTime = roundUpToMinute(scheduledTime), createdAt = clock.instant())
    val taskId = taskDao.insert(task)
    reminderSync.sync()
    return taskId
  }

  /** Changes only the title, so a Snoozed Task keeps its Snooze. */
  suspend fun rename(taskId: Long, title: String) {
    taskDao.rename(taskId, title)
    reminderSync.sync()
  }

  /** Gives the Task a new time the user picked, which ends any Snooze. */
  suspend fun reschedule(taskId: Long, title: String, scheduledTime: Instant) {
    taskDao.reschedule(taskId, title, roundUpToMinute(scheduledTime))
    reminderSync.sync()
  }

  /**
   * Skips one Instance of a series: it goes, and the next one takes its place. An Instance already Completed (say, from
   * its Reminder while its form was open) stays, because its Done already created the next one.
   */
  suspend fun skip(taskId: Long) {
    database.withTransaction(block = { -> skipAndContinueSeries(taskId) })
    reminderSync.sync()
  }

  suspend fun delete(taskId: Long) {
    taskDao.delete(taskId)
    reminderSync.sync()
  }

  /** The user responded to the Reminder: it stops ringing and stays in the shade. The Task itself doesn't change. */
  suspend fun silence(taskId: Long) {
    reminderSync.silence(taskId)
  }

  suspend fun snooze(taskId: Long, duration: SnoozeDuration) {
    val snoozedUntil = roundUpToMinute(snoozedUntilOf(duration, clock.instant()))
    taskDao.markSnoozed(taskId, snoozedUntil = snoozedUntil)
    reminderSync.sync()
  }

  /** Completing an Instance also creates the next one, in the same transaction, so a second Done can't create two. */
  suspend fun complete(taskId: Long) {
    database.withTransaction(block = { -> completeAndContinueSeries(taskId) })
    reminderSync.sync()
  }

  private suspend fun completeAndContinueSeries(taskId: Long) {
    val task = taskDao.findTask(taskId) ?: return
    val completedCount = taskDao.markCompleted(taskId, completedAt = clock.instant())
    val isNewlyCompleted = completedCount == 1
    if (isNewlyCompleted) createInstanceAfter(task)
  }

  private suspend fun skipAndContinueSeries(taskId: Long) {
    val task = taskDao.findTask(taskId) ?: return
    val deletedCount = taskDao.deleteUnlessCompleted(taskId)
    val isNewlySkipped = deletedCount == 1
    if (isNewlySkipped) createInstanceAfter(task)
  }

  /** Creates the Instance that follows [instance] in its series, if it has one. */
  private suspend fun createInstanceAfter(instance: Task) {
    val templateId = instance.templateId ?: return
    val template = templateDao.findTemplate(templateId) ?: return
    val nextInstanceTime = nextInstanceTimeOf(template, instance, clock.instant(), clock.zone) ?: return
    insertInstance(template, nextInstanceTime)
  }

  private suspend fun insertInstance(template: Template, scheduledTime: Instant) {
    val instance = Task(title = template.title, scheduledTime = scheduledTime, templateId = template.id, createdAt = clock.instant())
    taskDao.insert(instance)
  }
}

/** The time itself when it is already a whole minute, otherwise the start of the next minute. */
private fun roundUpToMinute(time: Instant): Instant {
  val startOfMinute = time.truncatedTo(ChronoUnit.MINUTES)
  val isWholeMinute = startOfMinute == time
  if (isWholeMinute) return time
  return startOfMinute.plus(1, ChronoUnit.MINUTES)
}
