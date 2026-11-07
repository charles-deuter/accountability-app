package dev.accountabilityapp.ui.reminder

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.data.TaskStatus
import dev.accountabilityapp.reminders.CHANNEL_ID
import dev.accountabilityapp.reminders.EXTRA_TASK_ID
import dev.accountabilityapp.reminders.SILENCED_CHANNEL_ID
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

/** Leaving the takeover silences its Reminder. This runs the real Activity on the app's own container. */
@RunWith(AndroidJUnit4::class)
class ReminderActivityTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val taskService = appContainerFrom(context).taskService
  private val notificationManager = shadowOf(context.getSystemService(NotificationManager::class.java))

  @Test
  fun theTakeover_keepsTheScreenOn_soTheScreenTimingOutDoesntCountAsLeaving() {
    val medsId = addDueTask()

    ActivityScenario.launch<ReminderActivity>(takeoverIntentOf(medsId)).use({ scenario ->
      scenario.onActivity({ activity ->
        val windowFlags = activity.window.attributes.flags
        assertTrue(windowFlags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
      })
    })
  }

  @Test
  fun leavingTheTakeover_stopsTheRinging_keepsTheReminder_andAppliesNoSnooze() {
    val medsId = addDueTask()

    ActivityScenario.launch<ReminderActivity>(takeoverIntentOf(medsId)).use({ scenario ->
      scenario.moveToState(Lifecycle.State.CREATED)

      waitForChannel(SILENCED_CHANNEL_ID)
      assertEquals(listOf(SILENCED_CHANNEL_ID), channelsOfShownReminders())
    })
    val task = runBlocking(block = { -> taskService.findTask(medsId) })
    assertEquals(TaskStatus.PENDING, task?.status)
    assertEquals(0, task?.snoozeCount)
  }

  @Test
  fun rotatingTheTakeover_keepsItRinging() {
    val medsId = addDueTask()

    ActivityScenario.launch<ReminderActivity>(takeoverIntentOf(medsId)).use({ scenario ->
      scenario.recreate()
      shadowOf(Looper.getMainLooper()).idle()
      Thread.sleep(200)

      assertEquals(listOf(CHANNEL_ID), channelsOfShownReminders())
    })
  }

  private fun addDueTask(): Long {
    val aMinuteAgo = Instant.now().minus(Duration.ofMinutes(1))
    return runBlocking(block = { -> taskService.add(title = "Take meds", scheduledTime = aMinuteAgo) })
  }

  private fun takeoverIntentOf(taskId: Long): Intent = Intent(context, ReminderActivity::class.java).putExtra(EXTRA_TASK_ID, taskId)

  private fun channelsOfShownReminders(): List<String> =
    notificationManager.allNotifications.map({ reminder: Notification -> reminder.channelId })

  /** Silencing runs off the main thread so it can outlive the Activity, so poll briefly for its result. */
  private fun waitForChannel(channelId: String) {
    val deadline = System.currentTimeMillis() + 5_000
    while (System.currentTimeMillis() < deadline) {
      shadowOf(Looper.getMainLooper()).idle()
      if (channelsOfShownReminders() == listOf(channelId)) return
      Thread.sleep(20)
    }
  }
}
