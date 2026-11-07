package dev.accountabilityapp

import android.app.Application
import android.content.Context
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.TaskService
import dev.accountabilityapp.reminders.ReminderAlarm
import dev.accountabilityapp.reminders.ReminderNotifications
import dev.accountabilityapp.reminders.ReminderSync
import dev.accountabilityapp.reminders.createReminderChannels
import java.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AccountabilityApplication : Application() {
  val container: AppContainer by lazy({ -> AppContainer(this) })

  override fun onCreate() {
    super.onCreate()
    createReminderChannels(this)
  }
}

/**
 * App-wide singletons, built by hand: the app is small enough not to need a DI framework. Tests pass their own [clock] and
 * an in-memory [database] to run the real wiring.
 */
class AppContainer(
  context: Context,
  val clock: Clock = Clock.systemDefaultZone(),
  val database: AppDatabase = AppDatabase.open(context),
) {
  /** For work that must outlive a screen, such as a broadcast receiver's database read. */
  val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  val reminderSync: ReminderSync =
    ReminderSync(database.taskDao(), clock, ReminderAlarm(context), ReminderNotifications(context))
  val taskService: TaskService = TaskService(database.taskDao(), clock, reminderSync)
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
