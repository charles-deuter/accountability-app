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
   * The Task Form shows a Task's Fire time, so a [scheduledTime] that matches it means the user left the time alone: the
   * Task keeps its Snooze. Any other time ends the Snooze.
   */
  suspend fun update(taskId: Long, title: String, scheduledTime: Instant) {
    val task = taskDao.findTask(taskId) ?: return
    if (isSameMinute(scheduledTime, fireTimeOf(task))) {
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

  suspend fun snooze(taskId: Long, duration: SnoozeDuration) {
    taskDao.markSnoozed(taskId, snoozedUntil = snoozedUntilOf(duration, clock.instant()))
    reminderSync.sync()
  }

  suspend fun complete(taskId: Long) {
    taskDao.markCompleted(taskId, completedAt = clock.instant())
    reminderSync.sync()
  }
}

/** The time picker works in whole minutes while a snoozed-until time has seconds, so confirming it unchanged drops them. */
private fun isSameMinute(first: Instant, second: Instant): Boolean =
  first.truncatedTo(ChronoUnit.MINUTES) == second.truncatedTo(ChronoUnit.MINUTES)
