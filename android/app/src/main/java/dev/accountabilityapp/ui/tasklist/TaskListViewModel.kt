package dev.accountabilityapp.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskService
import dev.accountabilityapp.data.TaskStatus
import dev.accountabilityapp.data.isOverdue
import dev.accountabilityapp.permissions.Grant
import dev.accountabilityapp.permissions.PermissionSource
import dev.accountabilityapp.permissions.missingGrantsFrom
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
)

/** Why the Task List shows its banner: a grant Reminders depend on is missing. */
enum class GrantWarning {
  /** Only exact alarms are missing: Reminders still ring, but Android may deliver them minutes late. */
  REMINDERS_MAY_RUN_LATE,
  REMINDERS_MAY_NOT_RING,
}

data class TaskListUiState(
  val items: List<TaskListItem> = emptyList(),
  /** Null while every grant is allowed, which hides the banner. */
  val grantWarning: GrantWarning? = null,
)

class TaskListViewModel(
  taskService: TaskService,
  clock: Clock,
  private val permissionSource: PermissionSource,
  private val sdkLevel: Int,
  tickInterval: Duration = 1.minutes,
) : ViewModel() {

  private val missingGrants = MutableStateFlow(missingGrantsFrom(permissionSource, sdkLevel))

  /**
   * Re-evaluates Overdue on every Task change and on every tick. The tick only runs while the state is collected (the
   * screen is visible), and restarts with a fresh time when the screen comes back, so nothing runs in the background.
   */
  val uiState: StateFlow<TaskListUiState> =
    combine(
        taskService.observeTaskList(),
        ticks(clock, tickInterval),
        missingGrants,
        transform = { tasks, now, missing ->
          TaskListUiState(items = tasks.map({ task -> taskListItemOf(task, now) }), grantWarning = grantWarningOf(missing))
        },
      )
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())

  /** The user can allow or revoke grants in system settings at any time, so the screen re-checks each time it resumes. */
  fun recheckGrants() {
    missingGrants.value = missingGrantsFrom(permissionSource, sdkLevel)
  }
}

private fun grantWarningOf(missingGrants: List<Grant>): GrantWarning? {
  val onlyExactAlarmsMissing = missingGrants == listOf(Grant.EXACT_ALARMS)
  return when {
    missingGrants.isEmpty() -> null
    onlyExactAlarmsMissing -> GrantWarning.REMINDERS_MAY_RUN_LATE
    else -> GrantWarning.REMINDERS_MAY_NOT_RING
  }
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
  )
}

/** Emits the current time immediately, then once per [interval]. */
private fun ticks(clock: Clock, interval: Duration): Flow<Instant> = flow({ ->
  while (true) {
    emit(clock.instant())
    delay(interval)
  }
})
