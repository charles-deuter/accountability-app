package dev.accountabilityapp.ui.taskform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.accountabilityapp.data.Recurrence
import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskService
import dev.accountabilityapp.data.limitedTitleOf
import dev.accountabilityapp.data.nextInstanceTimeOf
import dev.accountabilityapp.data.recurrenceOf
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
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
  /** Add Task only. Daily and Weekly use only the time of [scheduledTime]. */
  val repeat: Repeat = Repeat.ONCE,
  val weekdays: Set<DayOfWeek> = emptySet(),
  val isPaused: Boolean = false,
  /** True when the form paused the series itself because no days were ticked, so ticking one resumes it. */
  val isAutoPaused: Boolean = false,
  /** Set when the Task being edited is an Instance. */
  val series: Series? = null,
)

enum class Repeat {
  ONCE,
  DAILY,
  WEEKLY,
}

/** The series an Instance being edited belongs to, shown read-only. */
data class Series(val recurrence: Recurrence, val timeOfDay: LocalTime, val nextInstanceTime: Instant?)

/** A Weekly series with no days can never fire, so it stays paused until a day is ticked. */
fun isPauseLocked(state: TaskFormUiState): Boolean {
  val isWeekly = state.repeat == Repeat.WEEKLY
  return isWeekly && state.weekdays.isEmpty()
}

/** Adds a new Task, or edits the existing one when [taskId] is given. */
class TaskFormViewModel(private val taskService: TaskService, private val clock: Clock, private val taskId: Long? = null) : ViewModel() {

  private val isEditing = taskId != null
  private val today = clock.instant().atZone(clock.zone).dayOfWeek
  private val _uiState =
    MutableStateFlow(
      TaskFormUiState(scheduledTime = nextFullHour(clock), isEditing = isEditing, isLoading = isEditing, weekdays = setOf(today))
    )
  val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

  /** The time the form opened with. Saving the same time only renames the Task, so a Snooze survives. */
  private var loadedScheduledTime: Instant? = null

  init {
    if (taskId != null) {
      viewModelScope.launch(block = { -> loadForEditing(taskId) })
    }
  }

  private suspend fun loadForEditing(taskId: Long) {
    val task = taskService.findTask(taskId)
    if (task == null) {
      _uiState.update({ state -> state.copy(isLoading = false) })
      return
    }
    loadedScheduledTime = task.scheduledTime
    val series = seriesFrom(task)
    _uiState.update({ state -> state.copy(title = task.title, scheduledTime = task.scheduledTime, series = series, isLoading = false) })
  }

  private suspend fun seriesFrom(instance: Task): Series? {
    val templateId = instance.templateId ?: return null
    val template = taskService.findTemplate(templateId) ?: return null
    val nextInstanceTime = nextInstanceTimeOf(template, instance, clock.instant(), clock.zone)
    return Series(recurrenceOf(template), template.timeOfDay, nextInstanceTime)
  }

  fun onTitleChange(title: String) {
    _uiState.update({ state -> state.copy(title = limitedTitleOf(title, state.title), showTitleRequired = false) })
  }

  fun onScheduledTimeChange(scheduledTime: Instant) {
    _uiState.update({ state -> state.copy(scheduledTime = scheduledTime) })
  }

  fun onRepeatChange(repeat: Repeat) {
    _uiState.update({ state -> withPauseRules(state.copy(repeat = repeat)) })
  }

  fun onWeekdayToggle(day: DayOfWeek) {
    _uiState.update({ state ->
      val isTicked = day in state.weekdays
      val weekdays = if (isTicked) state.weekdays - day else state.weekdays + day
      withPauseRules(state.copy(weekdays = weekdays))
    })
  }

  /** Turning the switch by hand means the form no longer resumes the series on its own. */
  fun onPausedChange(isPaused: Boolean) {
    _uiState.update({ state ->
      val canChange = !isPauseLocked(state)
      if (canChange) state.copy(isPaused = isPaused, isAutoPaused = false) else state
    })
  }

  fun save() {
    val form = _uiState.value
    if (form.title.isBlank()) {
      _uiState.update({ state -> state.copy(showTitleRequired = true) })
      return
    }
    val title = form.title.trim()
    val isTimeChanged = form.scheduledTime != loadedScheduledTime
    val recurrence = chosenRecurrenceOf(form)
    viewModelScope.launch(block = { ->
      if (taskId == null && recurrence != null) {
        val timeOfDay = form.scheduledTime.atZone(clock.zone).toLocalTime()
        taskService.addRecurring(title = title, recurrence = recurrence, timeOfDay = timeOfDay, isActive = !form.isPaused)
      } else if (taskId == null) {
        taskService.add(title = title, scheduledTime = form.scheduledTime)
      } else if (isTimeChanged) {
        taskService.reschedule(taskId, title = title, scheduledTime = form.scheduledTime)
      } else {
        taskService.rename(taskId, title = title)
      }
      _uiState.update({ state -> state.copy(isFinished = true) })
    })
  }

  /** Deletes the Task being edited. The screen asks for confirmation before calling this. */
  fun delete() {
    val editedTaskId = taskId ?: return
    viewModelScope.launch(block = { ->
      taskService.delete(editedTaskId)
      _uiState.update({ state -> state.copy(isFinished = true) })
    })
  }

  /** Skips the Instance being edited: the next one in its series takes its place. The screen asks first. */
  fun skip() {
    val editedTaskId = taskId ?: return
    viewModelScope.launch(block = { ->
      taskService.skip(editedTaskId)
      _uiState.update({ state -> state.copy(isFinished = true) })
    })
  }
}

/**
 * Unticking the last day pauses the series, because it can't fire. Ticking a day again resumes it, but only if the form
 * paused it: a pause the user chose stays.
 */
private fun withPauseRules(state: TaskFormUiState): TaskFormUiState {
  val hasNoDays = isPauseLocked(state)
  val shouldAutoPause = hasNoDays && !state.isPaused
  if (shouldAutoPause) return state.copy(isPaused = true, isAutoPaused = true)
  val shouldAutoResume = !hasNoDays && state.isAutoPaused
  if (shouldAutoResume) return state.copy(isPaused = false, isAutoPaused = false)
  return state
}

/** Null for a one-off Task. */
private fun chosenRecurrenceOf(form: TaskFormUiState): Recurrence? =
  when (form.repeat) {
    Repeat.ONCE -> null
    Repeat.DAILY -> Recurrence.Daily
    Repeat.WEEKLY -> Recurrence.Weekly(form.weekdays)
  }

/** New Tasks default to the top of the next hour, so the picker starts somewhere sensible. */
private fun nextFullHour(clock: Clock): Instant = clock.instant().truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
