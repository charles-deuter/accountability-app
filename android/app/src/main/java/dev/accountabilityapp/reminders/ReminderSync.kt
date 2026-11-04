package dev.accountabilityapp.reminders

import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskDao
import dev.accountabilityapp.data.fireTimeOf
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Brings the system's reminder state in line with the database. Every trigger calls [sync]: a Task change, the alarm
 * firing, boot, clock changes, an app update, and app launch. Because it recomputes everything from the database, no
 * caller has to know what the previous alarm was.
 */
class ReminderSync(
  private val taskDao: TaskDao,
  private val clock: Clock,
  private val alarm: ReminderAlarm,
  private val notifications: ReminderNotifications,
) {

  /** Triggers can overlap (a launch while a receiver runs), so one sync at a time: two can't both post a Reminder. */
  private val mutex = Mutex()

  suspend fun sync() {
    mutex.withLock(action = { -> syncNow() })
  }

  private suspend fun syncNow() {
    val now = clock.instant()
    val openTasks = taskDao.findOpenTasks()

    // Only Tasks not already in the shade are posted, so a Reminder that is showing never rings twice.
    val shownTaskIds = notifications.shownTaskIds()
    val dueTasks = openTasks.filter({ task -> isDue(task, now) })
    for (task in dueTasks) {
      if (task.id !in shownTaskIds) {
        notifications.show(task)
      }
    }
    // A shown Reminder whose Task is no longer due (completed, deleted, or moved later) leaves the shade.
    val dueTaskIds = dueTasks.map({ task -> task.id }).toSet()
    for (shownTaskId in shownTaskIds) {
      if (shownTaskId !in dueTaskIds) {
        notifications.cancel(shownTaskId)
      }
    }

    val nextFireTime = nextFireTimeOf(openTasks, now)
    if (nextFireTime == null) {
      alarm.cancel()
    } else {
      alarm.scheduleAt(nextFireTime)
    }
  }
}

private fun isDue(task: Task, now: Instant): Boolean = !fireTimeOf(task).isAfter(now)

/** The soonest Fire time still in the future, or null when there is nothing left to remind about. */
private fun nextFireTimeOf(openTasks: List<Task>, now: Instant): Instant? {
  val futureFireTimes = openTasks.map(::fireTimeOf).filter({ fireTime -> fireTime.isAfter(now) })
  return futureFireTimes.minOrNull()
}
