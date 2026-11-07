package dev.accountabilityapp.ui.snooze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.accountabilityapp.data.SnoozeDuration
import dev.accountabilityapp.data.TaskService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SnoozeUiState(val isFinished: Boolean = false)

/** The Snooze picker for one Task. There is no way out without snoozing. */
class SnoozeViewModel(private val taskService: TaskService, private val taskId: Long) : ViewModel() {

  private val _uiState = MutableStateFlow(SnoozeUiState())
  val uiState: StateFlow<SnoozeUiState> = _uiState.asStateFlow()

  /** Set by the first choice, so leaving the screen afterwards doesn't snooze a second time. */
  private var hasSnoozed = false

  init {
    // Opening the picker is a response: the sound stops now, not once they've chosen.
    viewModelScope.launch(block = { -> taskService.silence(taskId) })
  }

  fun snooze(duration: SnoozeDuration) {
    if (hasSnoozed) return
    hasSnoozed = true
    viewModelScope.launch(block = { ->
      taskService.snooze(taskId, duration)
      _uiState.update({ state -> state.copy(isFinished = true) })
    })
  }

  /** Back, Home, or the screen turning off: a Reminder can't be dismissed, so leaving snoozes for 15 minutes. */
  fun leaveWithoutPicking() {
    snooze(SnoozeDuration.FIFTEEN_MINUTES)
  }
}
