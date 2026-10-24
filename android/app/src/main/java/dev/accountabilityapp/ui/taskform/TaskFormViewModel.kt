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
  val isSaved: Boolean = false,
)

class TaskFormViewModel(private val repository: TaskRepository, clock: Clock) : ViewModel() {

  private val _uiState = MutableStateFlow(TaskFormUiState(scheduledTime = nextFullHour(clock)))
  val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

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
    viewModelScope.launch(block = { ->
      repository.add(title = form.title.trim(), scheduledTime = form.scheduledTime)
      _uiState.update({ state -> state.copy(isSaved = true) })
    })
  }
}

/** New Tasks default to the top of the next hour, so the picker starts somewhere sensible. */
private fun nextFullHour(clock: Clock): Instant = clock.instant().truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
