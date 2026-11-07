package dev.accountabilityapp.reminders

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.MainActivity
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.data.TaskStatus
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

/** Tapping a Reminder's body is a response. This launches the intent the notification really carries, on the app's own container. */
@RunWith(AndroidJUnit4::class)
class ReminderTapTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val taskService = appContainerFrom(context).taskService
  private val notificationManager = shadowOf(context.getSystemService(NotificationManager::class.java))

  @Test
  fun tappingAReminder_stopsItsRinging_opensTheApp_andAppliesNoSnooze() {
    val medsId = addDueTask("Take meds")
    val tapIntent = shadowOf(reminderTitled("Take meds").contentIntent).savedIntent

    ActivityScenario.launch<MainActivity>(tapIntent).use({ _ ->
      waitForChannel("Take meds", SILENCED_CHANNEL_ID)
      assertEquals(SILENCED_CHANNEL_ID, reminderTitled("Take meds").channelId)
    })
    val task = runBlocking(block = { -> taskService.findTask(medsId) })
    assertEquals(TaskStatus.PENDING, task?.status)
    assertEquals(0, task?.snoozeCount)
  }

  @Test
  fun tappingOneReminder_leavesTheOtherRinging() {
    addDueTask("Take meds")
    addDueTask("Call dentist")
    val tapIntent = shadowOf(reminderTitled("Take meds").contentIntent).savedIntent

    ActivityScenario.launch<MainActivity>(tapIntent).use({ _ ->
      waitForChannel("Take meds", SILENCED_CHANNEL_ID)
      assertEquals(CHANNEL_ID, reminderTitled("Call dentist").channelId)
    })
  }

  /** Android reuses an open task whose root intent matches, without delivering the new one, so two Reminders can't match. */
  @Test
  fun twoRemindersTapIntents_differToAndroid() {
    addDueTask("Take meds")
    addDueTask("Call dentist")

    val medsIntent = shadowOf(reminderTitled("Take meds").contentIntent).savedIntent
    val dentistIntent = shadowOf(reminderTitled("Call dentist").contentIntent).savedIntent

    assertFalse(medsIntent.filterEquals(dentistIntent))
  }

  @Test
  fun launchingTheAppNormally_silencesNothing() {
    addDueTask("Take meds")

    ActivityScenario.launch(MainActivity::class.java).use({ _ ->
      shadowOf(Looper.getMainLooper()).idle()
      Thread.sleep(200)
      assertEquals(CHANNEL_ID, reminderTitled("Take meds").channelId)
    })
  }

  private fun addDueTask(title: String): Long {
    val aMinuteAgo = Instant.now().minus(Duration.ofMinutes(1))
    return runBlocking(block = { -> taskService.add(title = title, scheduledTime = aMinuteAgo) })
  }

  private fun reminderTitled(title: String): Notification =
    notificationManager.allNotifications.single({ reminder -> reminder.extras.getCharSequence(Notification.EXTRA_TITLE).toString() == title })

  /** Silencing runs off the main thread, so poll briefly for its result. */
  private fun waitForChannel(title: String, channelId: String) {
    val deadline = System.currentTimeMillis() + 5_000
    while (System.currentTimeMillis() < deadline) {
      shadowOf(Looper.getMainLooper()).idle()
      if (reminderTitled(title).channelId == channelId) return
      Thread.sleep(20)
    }
  }
}
