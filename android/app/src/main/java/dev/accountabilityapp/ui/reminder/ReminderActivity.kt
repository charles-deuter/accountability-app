package dev.accountabilityapp.ui.reminder

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.reminders.EXTRA_TASK_ID
import dev.accountabilityapp.theme.AccountabilityAppTheme
import dev.accountabilityapp.ui.snooze.snoozePickerIntentFrom
import kotlinx.coroutines.launch

/**
 * The lock-screen takeover, launched by a Reminder's full-screen intent: it shows without unlocking and turns the screen
 * on (see the manifest). It appears once per firing, and closes when the user leaves it or when the ringing limit is
 * reached. Either way the ringing stops and the Reminder stays in the shade.
 */
class ReminderActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Leaving stops the ringing, and the screen timing out would count as leaving: an unattended phone would go quiet in seconds.
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    val taskId = taskIdFrom(intent)
    val taskService = appContainerFrom(this).taskService
    val factory = viewModelFactory { initializer { ReminderViewModel(taskService, taskId) } }
    val viewModel = ViewModelProvider(this, factory)[ReminderViewModel::class.java]

    val openSnoozePicker: () -> Unit = { ->
      startActivity(snoozePickerIntentFrom(this, taskId))
      finish()
    }
    setContent {
      AccountabilityAppTheme { ReminderRoute(viewModel, onSnooze = openSnoozePicker, onFinished = { -> finish() }) }
    }
  }

  override fun onStop() {
    super.onStop()
    // Left (Home, power button, another app): close for good, so the takeover never comes back on its own.
    if (!isChangingConfigurations) {
      silenceReminder()
      finish()
    }
  }

  /** On the application's scope, because this Activity is about to finish. */
  private fun silenceReminder() {
    val taskId = taskIdFrom(intent)
    val container = appContainerFrom(this)
    container.applicationScope.launch(block = { -> container.taskService.silence(taskId) })
  }
}

private fun taskIdFrom(intent: Intent): Long = intent.getLongExtra(EXTRA_TASK_ID, -1)
