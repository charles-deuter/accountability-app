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

/**
 * Returns the app-wide [AppContainer] from any Context (an Activity, a Service, or the app itself).
 *
 * Goes through the application context: Activities are destroyed and recreated, while the Application lives as long as
 * the process and owns the container.
 */
fun appContainerFrom(context: Context): AppContainer {
  val application = context.applicationContext as AccountabilityApplication
  return application.container
}
