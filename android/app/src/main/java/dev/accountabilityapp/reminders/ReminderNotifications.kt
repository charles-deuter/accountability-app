package dev.accountabilityapp.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import dev.accountabilityapp.MainActivity
import dev.accountabilityapp.R
import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.fireTimeOf
import dev.accountabilityapp.ui.formatTime
import dev.accountabilityapp.ui.reminder.ReminderActivity
import dev.accountabilityapp.ui.snooze.snoozePickerIntentFrom
import java.time.Instant

/** Reminders in the notification shade: one notification per due Task, keyed by the Task's id. */
class ReminderNotifications(private val context: Context) {

  private val notificationManager = context.getSystemService(NotificationManager::class.java)

  /** Ids of the Tasks whose Reminder is in the shade right now. */
  fun shownTaskIds(): Set<Long> {
    val reminders = notificationManager.activeNotifications.filter({ shown -> shown.tag == REMINDER_TAG })
    return reminders.map({ shown -> shown.id.toLong() }).toSet()
  }

  /** Rings, and on a locked phone takes over the screen. [ringingSince] is kept on the notification: see [readRingingSince]. */
  fun show(task: Task, ringingSince: Instant) {
    // On a locked phone this takes over the screen; on one in use, Android shows a heads-up instead.
    val builder = reminderBuilderOf(task, CHANNEL_ID, ringingSince).setFullScreenIntent(takeoverIntentFrom(context, task.id), true)
    notificationManager.notify(REMINDER_TAG, notificationIdOf(task.id), builder.build())
  }

  /**
   * Android plays a channel's sound until the notification goes away or is updated to one with no sound, so this replaces it
   * in place on the silent channel, without the full-screen intent that would reopen the takeover. It isn't cancelled first:
   * Android applies these calls asynchronously, and a sync that found the Reminder missing would post it again, ringing.
   * Does nothing unless the Reminder is ringing.
   */
  fun silence(task: Task) {
    val ringingSince = readRingingSince()[task.id] ?: return
    val silenced = reminderBuilderOf(task, SILENCED_CHANNEL_ID, ringingSince).build()
    notificationManager.notify(REMINDER_TAG, notificationIdOf(task.id), silenced)
  }

  fun cancel(taskId: Long) {
    notificationManager.cancel(REMINDER_TAG, notificationIdOf(taskId))
  }

  /**
   * The Reminders on the ringing channel and when each started. This reads the system's notifications rather than memory
   * or the database, so it survives the process dying and needs no schema change.
   */
  fun readRingingSince(): Map<Long, Instant> {
    val ringing = notificationManager.activeNotifications.filter(::isRinging)
    return ringing.associate({ shown -> shown.id.toLong() to Instant.ofEpochMilli(shown.notification.`when`) })
  }

  private fun reminderBuilderOf(task: Task, channelId: String, ringingSince: Instant): NotificationCompat.Builder =
    NotificationCompat.Builder(context, channelId)
      .setWhen(ringingSince.toEpochMilli())
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
      .addAction(0, context.getString(R.string.done), actionIntentFrom(context, ACTION_DONE, task.id))
      .addAction(0, context.getString(R.string.snooze), snoozeIntentFrom(context, task.id))
      // Android 14+ lets an unlocked user swipe even an ongoing notification away: that snoozes instead.
      .setDeleteIntent(actionIntentFrom(context, ACTION_SWIPED, task.id))
}

/** Channel ids are permanent on the user's device: changing the sound later means a new id (ticket 16). */
const val CHANNEL_ID = "reminders"

/** A Reminder that has stopped ringing. Same shade section as a ringing one, just without the sound. */
const val SILENCED_CHANNEL_ID = "reminders_silenced"

private const val REMINDER_TAG = "reminder"

/**
 * Creates the Reminder channels; safe to call on every launch. Importance, sound and vibration are fixed once a channel
 * exists on a device (only the user can change them). The ringing channel is chosen for an alarm: high importance (heads-up
 * and full-screen), the default alarm sound played as an alarm, and vibration. The silenced channel has default importance,
 * so the Reminder stays with the alerting notifications rather than sinking to the silent section, and no sound or vibration.
 */
fun createReminderChannels(context: Context) {
  val alarmAudio = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
  val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
  val ringing = NotificationChannel(CHANNEL_ID, context.getString(R.string.reminder_channel_name), NotificationManager.IMPORTANCE_HIGH)
  ringing.setSound(alarmSound, alarmAudio)
  ringing.enableVibration(true)
  ringing.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

  val silenced = NotificationChannel(SILENCED_CHANNEL_ID, context.getString(R.string.silenced_reminder_channel_name), NotificationManager.IMPORTANCE_DEFAULT)
  silenced.setSound(null, null)
  silenced.enableVibration(false)
  silenced.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

  context.getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(ringing, silenced))
}

private fun isRinging(shown: StatusBarNotification): Boolean {
  val isReminder = shown.tag == REMINDER_TAG
  val isOnRingingChannel = shown.notification.channelId == CHANNEL_ID
  return isReminder && isOnRingingChannel
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
  val intent = snoozePickerIntentFrom(context, taskId)
  return PendingIntent.getActivity(context, notificationIdOf(taskId), intent, PendingIntent.FLAG_IMMUTABLE)
}
