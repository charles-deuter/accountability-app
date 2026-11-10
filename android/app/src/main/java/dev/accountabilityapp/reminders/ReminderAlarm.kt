package dev.accountabilityapp.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Instant

/**
 * The app's one AlarmManager alarm, set for the soonest moment a Reminder must start or stop ringing; when it fires,
 * [ReminderSync] sets it again for the next one. Setting it replaces the previous alarm, because both use the same
 * PendingIntent.
 */
class ReminderAlarm(private val context: Context) {

  private val alarmManager = context.getSystemService(AlarmManager::class.java)

  /**
   * Exact, and allowed while idle: fires on time in Doze, at most once per nine minutes while the phone is idle. Without
   * the exact-alarm permission (not pre-granted from Android 14; ticket 11 asks for it) it falls back to an inexact alarm,
   * which Android may deliver minutes late, rather than throwing.
   */
  fun scheduleAt(fireTime: Instant) {
    val triggerAtMillis = fireTime.toEpochMilli()
    val operation = alarmIntentFrom(context)
    if (canScheduleExactAlarms(alarmManager)) {
      alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
    } else {
      alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
    }
  }

  fun cancel() {
    alarmManager.cancel(alarmIntentFrom(context))
  }
}

fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean {
  // Before Android 12, exact alarms need no permission.
  val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  return !needsPermission || alarmManager.canScheduleExactAlarms()
}

private fun alarmIntentFrom(context: Context): PendingIntent {
  val intent = Intent(context, ReminderAlarmReceiver::class.java)
  return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}
