package dev.accountabilityapp.ui.taskform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.accountabilityapp.data.TaskRepository
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskFormUiState(
  val title: String = "",
  val scheduledTime: Instant,
  val showTitleRequired: Boolean = false,
  val isEditing: Boolean = false,
  val isFinished: Boolean = false,
  /** True while an existing Task's values are still being read into the form. */
  val isLoading: Boolean = false,
)

/** Adds a new Task, or edits the existing one when [taskId] is given. */
class TaskFormViewModel(private val repository: TaskRepository, clock: Clock, private val taskId: Long? = null) : ViewModel() {

  private val isEditing = taskId != null
  private val _uiState = MutableStateFlow(TaskFormUiState(scheduledTime = nextFullHour(clock), isEditing = isEditing, isLoading = isEditing))
  val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

  init {
    if (taskId != null) {
      viewModelScope.launch(block = { -> loadForEditing(taskId) })
    }
  }

  private suspend fun loadForEditing(taskId: Long) {
    val task = repository.findTask(taskId)
    if (task == null) {
      _uiState.update({ state -> state.copy(isLoading = false) })
      return
    }
    _uiState.update({ state -> state.copy(title = task.title, scheduledTime = task.scheduledTime, isLoading = false) })
  }

  fun onTitleChange(title: String) {
    _uiState.update({ state -> state.copy(title = title, showTitleRequired = false) })
  }

  fun onScheduledTimeChange(scheduledTime: Instant) {
    _uiState.update({ state -> state.copy(scheduledTime = scheduledTime) })
  }

  fun save() {
    val form = _uiState.value
    if (form.title.isBlank()) {
      _uiState.update({ state -> state.copy(showTitleRequired = true) })
      return
    }
    val title = form.title.trim()
    viewModelScope.launch(block = { ->
      if (taskId == null) {
        repository.add(title = title, scheduledTime = form.scheduledTime)
      } else {
        repository.update(taskId, title = title, scheduledTime = form.scheduledTime)
      }
      _uiState.update({ state -> state.copy(isFinished = true) })
    })
  }

  /** Deletes the Task being edited. The screen asks for confirmation before calling this. */
  fun delete() {
    val editedTaskId = taskId ?: return
    viewModelScope.launch(block = { ->
      repository.delete(editedTaskId)
      _uiState.update({ state -> state.copy(isFinished = true) })
    })
  }
}

/** New Tasks default to the top of the next hour, so the picker starts somewhere sensible. */
private fun nextFullHour(clock: Clock): Instant = clock.instant().truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
