package dev.accountabilityapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.accountabilityapp.appContainerFrom
import kotlinx.coroutines.launch

/**
 * Android erases every alarm on reboot, and an app update or clock change can leave the one alarm wrong. Each of those
 * broadcasts (see the manifest) re-runs the sync, which recomputes the alarm from the database and posts any Reminder
 * that came due while the phone was off.
 */
class RescheduleReceiver : BroadcastReceiver() {
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
