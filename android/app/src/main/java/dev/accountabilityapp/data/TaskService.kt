package dev.accountabilityapp.data

import dev.accountabilityapp.reminders.ReminderSync
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow

/** Task operations. Each write is followed by a [ReminderSync], so the alarm and notifications follow the database. */
class TaskService(private val taskDao: TaskDao, private val clock: Clock, private val reminderSync: ReminderSync) {

  /** Tasks shown in the Task List. */
  fun observeTaskList(): Flow<List<Task>> = taskDao.observeTaskList()

  suspend fun findTask(taskId: Long): Task? = taskDao.findTask(taskId)

  suspend fun add(title: String, scheduledTime: Instant): Long {
    val taskId = taskDao.insert(Task(title = title, scheduledTime = scheduledTime, createdAt = clock.instant()))
    reminderSync.sync()
    return taskId
  }

  /**
   * A [scheduledTime] that matches the Task's own means the user left the time alone: the Task keeps its Snooze. Any
   * other time ends the Snooze.
   */
  suspend fun update(taskId: Long, title: String, scheduledTime: Instant) {
    val task = taskDao.findTask(taskId) ?: return
    if (isSameMinute(scheduledTime, task.scheduledTime)) {
      taskDao.rename(taskId, title)
    } else {
      taskDao.update(taskId, title, scheduledTime)
    }
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
    taskDao.markSnoozed(taskId, snoozedUntil = snoozedUntilOf(duration, clock.instant()))
    reminderSync.sync()
  }

  suspend fun complete(taskId: Long) {
    taskDao.markCompleted(taskId, completedAt = clock.instant())
    reminderSync.sync()
  }
}

/** The time picker works in whole minutes while a Snooze leaves seconds in the scheduled time, so confirming it unchanged drops them. */
private fun isSameMinute(first: Instant, second: Instant): Boolean =
  first.truncatedTo(ChronoUnit.MINUTES) == second.truncatedTo(ChronoUnit.MINUTES)
