package dev.accountabilityapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: Task): Long

  /** Ordered by Fire time, which is the scheduled time. */
  @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY scheduledTime")
  fun observeTaskList(): Flow<List<Task>>

  /** Every Task that isn't Completed: the ones that can still need a Reminder. */
  @Query("SELECT * FROM tasks WHERE status != 'COMPLETED'")
  suspend fun findOpenTasks(): List<Task>

  @Query("SELECT * FROM tasks WHERE id = :taskId")
  suspend fun findTask(taskId: Long): Task?

  /**
   * Picking a new time ends any Snooze; the Snooze count stays as history. The original time stays too: for an Instance
   * it is the series slot the next Instance counts from.
   */
  @Query("UPDATE tasks SET title = :title, scheduledTime = :scheduledTime, status = 'PENDING' WHERE id = :taskId")
  suspend fun reschedule(taskId: Long, title: String, scheduledTime: Instant)

  @Query("UPDATE tasks SET title = :title WHERE id = :taskId")
  suspend fun rename(taskId: Long, title: String)

  @Query("DELETE FROM tasks WHERE id = :taskId")
  suspend fun delete(taskId: Long)

  /** Moves the scheduled time and leaves the original alone. One statement, so the count can't be lost between a read and a write. */
  @Query("UPDATE tasks SET status = 'SNOOZED', snoozeCount = snoozeCount + 1, scheduledTime = :snoozedUntil WHERE id = :taskId")
  suspend fun markSnoozed(taskId: Long, snoozedUntil: Instant)

  /** Returns how many Tasks it completed: 0 when the Task was already Completed or doesn't exist. */
  @Query("UPDATE tasks SET status = 'COMPLETED', completedAt = :completedAt WHERE id = :taskId AND status != 'COMPLETED'")
  suspend fun markCompleted(taskId: Long, completedAt: Instant): Int
}
