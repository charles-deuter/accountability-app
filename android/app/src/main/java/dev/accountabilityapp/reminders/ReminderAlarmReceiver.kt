package dev.accountabilityapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.accountabilityapp.appContainerFrom
import kotlinx.coroutines.launch

/**
 * Android calls this when the reminder alarm goes off, starting the app process if it was dead. A receiver gets about ten
 * seconds; `goAsync` keeps it alive while the database read runs off the main thread.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val container = appContainerFrom(context)
    val pendingResult = goAsync()
    container.applicationScope.launch(block = { ->
      try {
        container.reminderSync.sync()
      } finally {
        pendingResult.finish()
      }
    })
  }
}
