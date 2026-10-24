package dev.accountabilityapp.data

import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao, private val clock: Clock) {

  /** Tasks shown in the Task List. */
  fun observeTaskList(): Flow<List<Task>> = taskDao.observeTaskList()

  suspend fun add(title: String, scheduledTime: Instant): Long =
    taskDao.insert(Task(title = title, scheduledTime = scheduledTime, createdAt = clock.instant()))

  suspend fun complete(taskId: Long) = taskDao.markCompleted(taskId, completedAt = clock.instant())
}
