package dev.accountabilityapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.data.SnoozeDuration
import dev.accountabilityapp.data.TaskService
import kotlinx.coroutines.launch

const val ACTION_DONE = "dev.accountabilityapp.action.DONE"
const val ACTION_SWIPED = "dev.accountabilityapp.action.SWIPED"
const val EXTRA_TASK_ID = "dev.accountabilityapp.extra.TASK_ID"

/** Handles a Reminder notification's Done button and its being swiped away. */
class ReminderActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val container = appContainerFrom(context)
    val pendingResult = goAsync()
    container.applicationScope.launch(block = { ->
      try {
        handleReminderAction(intent, container.taskService)
      } finally {
        pendingResult.finish()
      }
    })
  }
}

/** Done completes the Task. Swiping away snoozes it for 30 minutes, because a Reminder can't be dismissed. */
suspend fun handleReminderAction(intent: Intent, taskService: TaskService) {
  val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
  if (taskId == -1L) return
  when (intent.action) {
    ACTION_DONE -> taskService.complete(taskId)
    ACTION_SWIPED -> taskService.snooze(taskId, SnoozeDuration.THIRTY_MINUTES)
  }
}
