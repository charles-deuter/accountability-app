package dev.accountabilityapp.ui.snooze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.reminders.EXTRA_TASK_ID
import dev.accountabilityapp.theme.AccountabilityAppTheme

/**
 * The Snooze picker, opened from a Reminder's Snooze button over whatever the user was doing. One tap snoozes and
 * returns them there; any other way out (Back, Home, the screen turning off) snoozes for 15 minutes.
 */
class SnoozeActivity : ComponentActivity() {

  private lateinit var viewModel: SnoozeViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
    val taskService = appContainerFrom(this).taskService
    val factory = viewModelFactory { initializer { SnoozeViewModel(taskService, taskId) } }
    viewModel = ViewModelProvider(this, factory)[SnoozeViewModel::class.java]

    onBackPressedDispatcher.addCallback(this, onBackPressed = { -> viewModel.leaveWithoutPicking() })
    setContent { AccountabilityAppTheme { SnoozeRoute(viewModel, onFinished = { -> finish() }) } }
  }

  override fun onStop() {
    super.onStop()
    // Leaving for anywhere else counts as an exit, but rotating the screen doesn't.
    if (!isChangingConfigurations) {
      viewModel.leaveWithoutPicking()
    }
  }
}
