package dev.accountabilityapp

import android.app.Application
import android.content.Context
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.TaskRepository
import java.time.Clock

class AccountabilityApplication : Application() {
  val container: AppContainer by lazy { AppContainer(this) }
}

/** App-wide singletons, built by hand: the app is small enough not to need a DI framework. */
class AppContainer(context: Context) {
  val clock: Clock = Clock.systemDefaultZone()
  val database: AppDatabase = AppDatabase.open(context)
  val taskRepository: TaskRepository = TaskRepository(database.taskDao(), clock)
}

val Context.appContainer: AppContainer
  get() = (this.applicationContext as AccountabilityApplication).container
