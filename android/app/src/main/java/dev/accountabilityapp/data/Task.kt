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

/** The moment this Task's Reminder goes off. Snoozing moves it (ticket 08). */
val Task.fireTime: Instant
  get() = this.scheduledTime

/** Overdue: the Fire time has passed and the Task isn't Completed. */
fun Task.isOverdue(now: Instant): Boolean = this.status != TaskStatus.COMPLETED && this.fireTime.isBefore(now)
