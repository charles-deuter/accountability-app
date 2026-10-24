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

  @Query("SELECT * FROM tasks WHERE id = :taskId")
  suspend fun findTask(taskId: Long): Task?

  @Query("UPDATE tasks SET title = :title, scheduledTime = :scheduledTime WHERE id = :taskId")
  suspend fun update(taskId: Long, title: String, scheduledTime: Instant)

  @Query("DELETE FROM tasks WHERE id = :taskId")
  suspend fun delete(taskId: Long)

  @Query("UPDATE tasks SET status = 'COMPLETED', completedAt = :completedAt WHERE id = :taskId")
  suspend fun markCompleted(taskId: Long, completedAt: Instant)
}
