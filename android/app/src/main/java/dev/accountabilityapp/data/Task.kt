package dev.accountabilityapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/** One thing the user has to do at a specific time. See CONTEXT.md for the domain terms. */
@Entity(tableName = "tasks")
data class Task(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  /**
   * The Task's Fire time, and the only time the alarm and notifications follow. A Snooze moves it. Always a whole
   * minute.
   */
  val scheduledTime: Instant,
  val originalScheduledTime: Instant = scheduledTime,
  /** Set when a Template created this Task (ticket 13). */
  val templateId: Long? = null,
  val status: TaskStatus = TaskStatus.PENDING,
  val snoozeCount: Int = 0,
  val completedAt: Instant? = null,
  val createdAt: Instant,
)

/** The longest a Task's title can be. A first try, so one number to change. */
const val TASK_TITLE_MAX_LENGTH = 100

/**
 * What a title becomes when it's edited from [current] to [edited]. Text inserted before the end of a title with no room is
 * refused, because keeping it would cut off the end of what the user already wrote. Anything else is shortened to the limit.
 * An emoji is two Chars, so one sitting across the limit goes whole, not cut in half.
 */
fun limitedTitleOf(edited: String, current: String): String {
  if (edited.length <= TASK_TITLE_MAX_LENGTH) return edited
  val isLonger = edited.length > current.length
  val isInsertedBeforeTheEnd = isLonger && !edited.startsWith(current)
  if (isInsertedBeforeTheEnd) return current
  val shortened = edited.take(TASK_TITLE_MAX_LENGTH)
  val endsInsideACharacter = shortened.last().isHighSurrogate()
  return if (endsInsideACharacter) shortened.dropLast(1) else shortened
}

enum class TaskStatus {
  PENDING,
  SNOOZED,
  COMPLETED,
}

/** Overdue: the Fire time has passed and the Task isn't Completed. */
fun isOverdue(task: Task, now: Instant): Boolean {
  val isCompleted = task.status == TaskStatus.COMPLETED
  val fireTimeHasPassed = task.scheduledTime.isBefore(now)
  return !isCompleted && fireTimeHasPassed
}
