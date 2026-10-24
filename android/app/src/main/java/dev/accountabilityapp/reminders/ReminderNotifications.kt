package dev.accountabilityapp.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import dev.accountabilityapp.MainActivity
import dev.accountabilityapp.R
import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.fireTimeOf
import dev.accountabilityapp.ui.formatTime
import dev.accountabilityapp.ui.reminder.ReminderActivity
import dev.accountabilityapp.ui.snooze.SnoozeActivity

/** Reminders in the notification shade: one notification per due Task, keyed by the Task's id. */
class ReminderNotifications(private val context: Context) {

  private val notificationManager = context.getSystemService(NotificationManager::class.java)

  /** Ids of the Tasks whose Reminder is in the shade right now. */
  fun shownTaskIds(): Set<Long> {
    val reminders = notificationManager.activeNotifications.filter({ shown -> shown.tag == REMINDER_TAG })
    return reminders.map({ shown -> shown.id.toLong() }).toSet()
  }

  fun show(task: Task) {
    val notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_reminder)
        .setContentTitle(task.title)
        .setContentText(formatTime(fireTimeOf(task)))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        // A Reminder can't be dismissed: the only ways out are its actions.
        .setOngoing(true)
        .setAutoCancel(false)
        .setOnlyAlertOnce(true)
        .setContentIntent(openAppIntentFrom(context))
        // On a locked phone this takes over the screen; on one in use, Android shows a heads-up instead.
        .setFullScreenIntent(takeoverIntentFrom(context, task.id), true)
        .addAction(0, context.getString(R.string.done), actionIntentFrom(context, ACTION_DONE, task.id))
        .addAction(0, context.getString(R.string.snooze), snoozeIntentFrom(context, task.id))
        // Android 14+ lets an unlocked user swipe even an ongoing notification away: that snoozes instead.
        .setDeleteIntent(actionIntentFrom(context, ACTION_SWIPED, task.id))
        .build()
    notificationManager.notify(REMINDER_TAG, notificationIdOf(task.id), notification)
  }

  fun cancel(taskId: Long) {
    notificationManager.cancel(REMINDER_TAG, notificationIdOf(taskId))
  }
}

/** Channel ids are permanent on the user's device: changing the sound later means a new id (ticket 16). */
const val CHANNEL_ID = "reminders"

private const val REMINDER_TAG = "reminder"

/**
 * Creates the Reminders channel; safe to call on every launch. Importance, sound and vibration are fixed once the channel
 * exists on a device (only the user can change them), so they are chosen here for an alarm: high importance (heads-up and
 * full-screen), the default alarm sound played as an alarm, and vibration.
 */
fun createReminderChannel(context: Context) {
  val alarmAudio = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
  val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
  val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.reminder_channel_name), NotificationManager.IMPORTANCE_HIGH)
  channel.setSound(alarmSound, alarmAudio)
  channel.enableVibration(true)
  channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
  context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

private fun notificationIdOf(taskId: Long): Int = taskId.toInt()

private fun openAppIntentFrom(context: Context): PendingIntent {
  val intent = Intent(context, MainActivity::class.java)
  return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}

private fun actionIntentFrom(context: Context, action: String, taskId: Long): PendingIntent {
  val intent = Intent(context, ReminderActionReceiver::class.java).setAction(action).putExtra(EXTRA_TASK_ID, taskId)
  // A distinct request code per Task, so each notification's intents carry its own Task id.
  return PendingIntent.getBroadcast(context, notificationIdOf(taskId), intent, PendingIntent.FLAG_IMMUTABLE)
}

private fun takeoverIntentFrom(context: Context, taskId: Long): PendingIntent {
  val intent = Intent(context, ReminderActivity::class.java).putExtra(EXTRA_TASK_ID, taskId)
  return PendingIntent.getActivity(context, notificationIdOf(taskId), intent, PendingIntent.FLAG_IMMUTABLE)
}

private fun snoozeIntentFrom(context: Context, taskId: Long): PendingIntent {
  val intent = Intent(context, SnoozeActivity::class.java).putExtra(EXTRA_TASK_ID, taskId)
  return PendingIntent.getActivity(context, notificationIdOf(taskId), intent, PendingIntent.FLAG_IMMUTABLE)
}
