package dev.accountabilityapp.data

import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao, private val clock: Clock) {

  /** Tasks shown in the Task List. */
  fun observeTaskList(): Flow<List<Task>> = taskDao.observeTaskList()

  suspend fun findTask(taskId: Long): Task? = taskDao.findTask(taskId)

  suspend fun add(title: String, scheduledTime: Instant): Long =
    taskDao.insert(Task(title = title, scheduledTime = scheduledTime, createdAt = clock.instant()))

  suspend fun update(taskId: Long, title: String, scheduledTime: Instant) = taskDao.update(taskId, title, scheduledTime)

  suspend fun delete(taskId: Long) = taskDao.delete(taskId)

  suspend fun complete(taskId: Long) = taskDao.markCompleted(taskId, completedAt = clock.instant())
}
