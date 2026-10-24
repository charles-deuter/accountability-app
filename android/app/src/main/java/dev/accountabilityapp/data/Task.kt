package dev.accountabilityapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/** One thing the user has to do at a specific time. See CONTEXT.md for the domain terms. */
@Entity(tableName = "tasks")
data class Task(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val scheduledTime: Instant,
  /** Set when a Template created this Task (ticket 13). */
  val templateId: Long? = null,
  val status: TaskStatus = TaskStatus.PENDING,
  val snoozeCount: Int = 0,
  val snoozedUntil: Instant? = null,
  val completedAt: Instant? = null,
  val createdAt: Instant,
)

enum class TaskStatus {
  PENDING,
  SNOOZED,
  COMPLETED,
}

/** The moment a Task's Reminder goes off. Snoozing moves it (ticket 08). */
fun fireTimeOf(task: Task): Instant = task.scheduledTime

/** Overdue: the Fire time has passed and the Task isn't Completed. */
fun isOverdue(task: Task, now: Instant): Boolean {
  val isCompleted = task.status == TaskStatus.COMPLETED
  val fireTimeHasPassed = fireTimeOf(task).isBefore(now)
  return !isCompleted && fireTimeHasPassed
}
