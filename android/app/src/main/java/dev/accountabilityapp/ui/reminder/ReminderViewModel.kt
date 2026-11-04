package dev.accountabilityapp.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.accountabilityapp.data.TaskService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReminderUiState(val title: String = "", val isFinished: Boolean = false)

/** The lock-screen takeover for one Task's Reminder. */
class ReminderViewModel(private val taskService: TaskService, private val taskId: Long) : ViewModel() {

  private val _uiState = MutableStateFlow(ReminderUiState())
  val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch(block = { -> loadTitle() })
  }

  private suspend fun loadTitle() {
    val task = taskService.findTask(taskId) ?: return
    _uiState.update({ state -> state.copy(title = task.title) })
  }

  fun complete() {
    viewModelScope.launch(block = { ->
      taskService.complete(taskId)
      _uiState.update({ state -> state.copy(isFinished = true) })
    })
  }
}
