package dev.accountabilityapp.reminders

import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskDao
import dev.accountabilityapp.data.fireTimeOf
import java.time.Clock
import java.time.Instant

/**
 * Brings the system's reminder state in line with the database. Every trigger calls [sync]: a Task change, the alarm
 * firing, and (ticket 10) boot, clock changes and app launch. Because it recomputes everything from the database, no
 * caller has to know what the previous alarm was.
 */
class ReminderSync(private val taskDao: TaskDao, private val clock: Clock, private val alarm: ReminderAlarm) {

  suspend fun sync() {
    val now = clock.instant()
    val openTasks = taskDao.findOpenTasks()
    val nextFireTime = nextFireTimeOf(openTasks, now)
    if (nextFireTime == null) {
      alarm.cancel()
    } else {
      alarm.scheduleAt(nextFireTime)
    }
  }
}

/** The soonest Fire time still in the future, or null when there is nothing left to remind about. */
private fun nextFireTimeOf(openTasks: List<Task>, now: Instant): Instant? {
  val futureFireTimes = openTasks.map(::fireTimeOf).filter({ fireTime -> fireTime.isAfter(now) })
  return futureFireTimes.minOrNull()
}
