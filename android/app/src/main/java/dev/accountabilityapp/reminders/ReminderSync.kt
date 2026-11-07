package dev.accountabilityapp.reminders

import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskDao
import dev.accountabilityapp.data.fireTimeOf
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
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

  /** Stops one showing Reminder ringing. Does nothing if it isn't showing or is already silent. */
  suspend fun silence(taskId: Long) {
    mutex.withLock(action = { ->
      val task = taskDao.findTask(taskId)
      if (task != null) {
        notifications.silence(task)
      }
      syncNow()
    })
  }

  private suspend fun syncNow() {
    val now = clock.instant()
    val openTasks = taskDao.findOpenTasks()

    // Only Tasks not already in the shade are posted, so a Reminder that is showing never rings twice.
    val shownTaskIds = notifications.shownTaskIds()
    val dueTasks = openTasks.filter({ task -> isDue(task, now) })
    val newTasks = dueTasks.filter({ task -> task.id !in shownTaskIds })
    for (task in newTasks) {
      notifications.show(task, ringingSince = now)
    }
    // A shown Reminder whose Task is no longer due (completed, deleted, or moved later) leaves the shade.
    val dueTaskIds = dueTasks.map({ task -> task.id }).toSet()
    for (shownTaskId in shownTaskIds) {
      if (shownTaskId !in dueTaskIds) {
        notifications.cancel(shownTaskId)
      }
    }

    // A Reminder nobody has responded to stops ringing at its limit. It stays in the shade, because it can't be dismissed.
    // Android posts asynchronously, so the Reminders just shown may not be readable yet and are added by hand.
    val newTasksRingingSince = newTasks.associate({ task -> task.id to now })
    val ringingSince = notifications.readRingingSince() + newTasksRingingSince
    val ringDeadlines = ringingSince.mapValues({ (_, startedAt) -> ringDeadlineOf(startedAt) })
    for (task in dueTasks) {
      val ringDeadline = ringDeadlines[task.id] ?: continue
      val hasRungLongEnough = !ringDeadline.isAfter(now)
      if (hasRungLongEnough) {
        notifications.silence(task)
      }
    }

    val nextAlarmTime = nextAlarmTimeOf(openTasks, ringDeadlines.values, now)
    if (nextAlarmTime == null) {
      alarm.cancel()
    } else {
      alarm.scheduleAt(nextAlarmTime)
    }
  }
}

/** How long a Reminder rings with no response. Doze may stretch this while the phone is idle. */
val RING_LIMIT = 5.minutes

private fun ringDeadlineOf(ringingSince: Instant): Instant = ringingSince.plus(RING_LIMIT.toJavaDuration())

private fun isDue(task: Task, now: Instant): Boolean = !fireTimeOf(task).isAfter(now)

/** The soonest moment still in the future when a Reminder must start or stop ringing, or null when there is none. */
private fun nextAlarmTimeOf(openTasks: List<Task>, ringDeadlines: Collection<Instant>, now: Instant): Instant? {
  val alarmTimes = openTasks.map(::fireTimeOf) + ringDeadlines
  val futureAlarmTimes = alarmTimes.filter({ alarmTime -> alarmTime.isAfter(now) })
  return futureAlarmTimes.minOrNull()
}
