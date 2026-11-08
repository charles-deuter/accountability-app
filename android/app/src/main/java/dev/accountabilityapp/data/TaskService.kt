package dev.accountabilityapp.data

import dev.accountabilityapp.reminders.ReminderSync
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow

/**
 * Task operations. Each write is followed by a [ReminderSync], so the alarm and notifications follow the database. Every
 * time a Task is given is rounded up to a whole minute.
 */
class TaskService(private val taskDao: TaskDao, private val clock: Clock, private val reminderSync: ReminderSync) {

  /** Tasks shown in the Task List. */
  fun observeTaskList(): Flow<List<Task>> = taskDao.observeTaskList()

  suspend fun findTask(taskId: Long): Task? = taskDao.findTask(taskId)

  suspend fun add(title: String, scheduledTime: Instant): Long {
    val task = Task(title = title, scheduledTime = roundUpToMinute(scheduledTime), createdAt = clock.instant())
    val taskId = taskDao.insert(task)
    reminderSync.sync()
    return taskId
  }

  /**
   * A [scheduledTime] that matches the Task's own means the user left the time alone: the Task keeps its Snooze. Any
   * other time ends the Snooze.
   */
  suspend fun update(taskId: Long, title: String, scheduledTime: Instant) {
    val task = taskDao.findTask(taskId) ?: return
    val newTime = roundUpToMinute(scheduledTime)
    if (newTime == task.scheduledTime) {
      taskDao.rename(taskId, title)
    } else {
      taskDao.update(taskId, title, newTime)
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
    val snoozedUntil = roundUpToMinute(snoozedUntilOf(duration, clock.instant()))
    taskDao.markSnoozed(taskId, snoozedUntil = snoozedUntil)
    reminderSync.sync()
  }

  suspend fun complete(taskId: Long) {
    taskDao.markCompleted(taskId, completedAt = clock.instant())
    reminderSync.sync()
  }
}

/** The time itself when it is already a whole minute, otherwise the start of the next minute. */
private fun roundUpToMinute(time: Instant): Instant {
  val startOfMinute = time.truncatedTo(ChronoUnit.MINUTES)
  val isWholeMinute = startOfMinute == time
  if (isWholeMinute) return time
  return startOfMinute.plus(1, ChronoUnit.MINUTES)
}
