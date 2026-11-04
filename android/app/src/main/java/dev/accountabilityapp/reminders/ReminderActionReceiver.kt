package dev.accountabilityapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.accountabilityapp.appContainerFrom
import kotlinx.coroutines.launch

const val ACTION_DONE = "dev.accountabilityapp.action.DONE"
const val EXTRA_TASK_ID = "dev.accountabilityapp.extra.TASK_ID"

/** Handles the buttons on a Reminder notification. */
class ReminderActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
    val isDone = intent.action == ACTION_DONE
    if (!isDone || taskId == -1L) return
    val container = appContainerFrom(context)
    val pendingResult = goAsync()
    container.applicationScope.launch(block = { ->
      try {
        container.taskService.complete(taskId)
      } finally {
        pendingResult.finish()
      }
    })
  }
}
