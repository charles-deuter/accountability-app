package dev.accountabilityapp.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.accountabilityapp.data.TaskService
import dev.accountabilityapp.data.fireTimeOf
import dev.accountabilityapp.data.isOverdue
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class TaskListItem(val id: Long, val title: String, val fireTime: Instant, val isOverdue: Boolean)

data class TaskListUiState(val items: List<TaskListItem> = emptyList())

class TaskListViewModel(taskService: TaskService, clock: Clock, tickInterval: Duration = 1.minutes) : ViewModel() {

  /**
   * Re-evaluates Overdue on every Task change and on every tick. The tick only runs while the state is collected (the
   * screen is visible), and restarts with a fresh time when the screen comes back, so nothing runs in the background.
   */
  val uiState: StateFlow<TaskListUiState> =
    combine(
        taskService.observeTaskList(),
        ticks(clock, tickInterval),
        transform = { tasks, now ->
          TaskListUiState(tasks.map({ task -> TaskListItem(task.id, task.title, fireTimeOf(task), isOverdue(task, now)) }))
        },
      )
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())
}

/** Emits the current time immediately, then once per [interval]. */
private fun ticks(clock: Clock, interval: Duration): Flow<Instant> = flow({ ->
  while (true) {
    emit(clock.instant())
    delay(interval)
  }
})
