package dev.accountabilityapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: Task): Long

  @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY scheduledTime")
  fun observeTaskList(): Flow<List<Task>>

  /** Every Task that isn't Completed: the ones that can still need a Reminder. */
  @Query("SELECT * FROM tasks WHERE status != 'COMPLETED'")
  suspend fun findOpenTasks(): List<Task>

  @Query("SELECT * FROM tasks WHERE id = :taskId")
  suspend fun findTask(taskId: Long): Task?

  /** Editing picks a new time, so it ends any Snooze; the Snooze count stays as history. */
  @Query(
    "UPDATE tasks SET title = :title, scheduledTime = :scheduledTime, status = 'PENDING', snoozedUntil = NULL WHERE id = :taskId"
  )
  suspend fun update(taskId: Long, title: String, scheduledTime: Instant)

  @Query("DELETE FROM tasks WHERE id = :taskId")
  suspend fun delete(taskId: Long)

  /** One statement, so the count can't be lost between a read and a write. */
  @Query("UPDATE tasks SET status = 'SNOOZED', snoozeCount = snoozeCount + 1, snoozedUntil = :snoozedUntil WHERE id = :taskId")
  suspend fun markSnoozed(taskId: Long, snoozedUntil: Instant)

  @Query("UPDATE tasks SET status = 'COMPLETED', completedAt = :completedAt WHERE id = :taskId")
  suspend fun markCompleted(taskId: Long, completedAt: Instant)
}
