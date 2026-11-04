package dev.accountabilityapp.ui.reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.reminders.EXTRA_TASK_ID
import dev.accountabilityapp.theme.AccountabilityAppTheme
import dev.accountabilityapp.ui.snooze.snoozePickerIntentFrom

/**
 * The lock-screen takeover, launched by a Reminder's full-screen intent: it shows without unlocking and turns the screen
 * on (see the manifest). It appears once per firing; leaving it closes it, and the Reminder stays in the shade.
 */
class ReminderActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
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
      finish()
    }
  }
}
