package dev.accountabilityapp.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.Instant

/**
 * The app's one AlarmManager alarm. Only the soonest Fire time has an alarm; when it fires, [ReminderSync] sets it again
 * for the next one. Setting it replaces the previous alarm, because both use the same PendingIntent.
 */
class ReminderAlarm(private val context: Context) {

  private val alarmManager = context.getSystemService(AlarmManager::class.java)

  /** Exact, and allowed while idle: fires on time in Doze, at most once per nine minutes while the phone is idle. */
  fun scheduleAt(fireTime: Instant) {
    val triggerAtMillis = fireTime.toEpochMilli()
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarmIntentFrom(context))
  }

  fun cancel() {
    alarmManager.cancel(alarmIntentFrom(context))
  }
}

private fun alarmIntentFrom(context: Context): PendingIntent {
  val intent = Intent(context, ReminderAlarmReceiver::class.java)
  return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}
