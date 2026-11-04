package dev.accountabilityapp

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.data.Task
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

/**
 * Force stop erases the app's alarms, and nothing reaches the app again until the user opens it. Opening it must
 * recover: this uses the app's own container, as a real launch does.
 */
@RunWith(AndroidJUnit4::class)
class LaunchRecoveryTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val notificationManager = shadowOf(context.getSystemService(NotificationManager::class.java))

  @Test
  fun launchingTheApp_remindsAboutTasksThatCameDueWhileItWasForceStopped() {
    val anHourAgo = Instant.now().minus(Duration.ofHours(1))
    val taskDao = appContainerFrom(context).database.taskDao()
    runBlocking(block = { -> taskDao.insert(Task(title = "Take meds", scheduledTime = anHourAgo, createdAt = anHourAgo)) })

    ActivityScenario.launch(MainActivity::class.java).use({ _ ->
      val reminders = waitForReminders()
      assertEquals(listOf("Take meds"), reminders.map({ reminder -> reminder.extras.getCharSequence(Notification.EXTRA_TITLE).toString() }))
    })
  }

  /** The launch sync reads the database off the main thread, so poll briefly for its result. */
  private fun waitForReminders(): List<Notification> {
    val deadline = System.currentTimeMillis() + 5_000
    while (System.currentTimeMillis() < deadline) {
      shadowOf(Looper.getMainLooper()).idle()
      val reminders = notificationManager.allNotifications
      if (reminders.isNotEmpty()) return reminders
      Thread.sleep(20)
    }
    return emptyList()
  }
}
