package dev.accountabilityapp.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskService
import dev.accountabilityapp.data.TaskStatus
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

data class TaskListItem(
  val id: Long,
  val title: String,
  val fireTime: Instant,
  val isOverdue: Boolean,
  val snoozeCount: Int = 0,
  /** Set only while the Task is Snoozed. */
  val snoozedUntil: Instant? = null,
  /** An Instance of a Template, shown with a repeat icon. */
  val isRecurring: Boolean = false,
)

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
          TaskListUiState(tasks.map({ task -> taskListItemOf(task, now) }))
        },
      )
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())
}

private fun taskListItemOf(task: Task, now: Instant): TaskListItem {
  val isSnoozed = task.status == TaskStatus.SNOOZED
  val snoozedUntil = if (isSnoozed) task.scheduledTime else null
  return TaskListItem(
    id = task.id,
    title = task.title,
    fireTime = task.scheduledTime,
    isOverdue = isOverdue(task, now),
    snoozeCount = task.snoozeCount,
    snoozedUntil = snoozedUntil,
    isRecurring = task.templateId != null,
  )
}

/** Emits the current time immediately, then once per [interval]. */
private fun ticks(clock: Clock, interval: Duration): Flow<Instant> = flow({ ->
  while (true) {
    emit(clock.instant())
    delay(interval)
  }
})
