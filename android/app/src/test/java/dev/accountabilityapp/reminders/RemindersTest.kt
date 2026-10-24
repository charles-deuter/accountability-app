package dev.accountabilityapp.reminders

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.SettableClock
import dev.accountabilityapp.data.AppDatabase
import dev.accountabilityapp.data.SnoozeDuration
import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskStatus
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlarmManager

/**
 * Reminders end to end on the JVM: real Room, real wiring, and Robolectric's AlarmManager and NotificationManager standing
 * in for the system.
 */
@RunWith(AndroidJUnit4::class)
class RemindersTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val clock = SettableClock(Instant.parse("2026-10-24T12:00:00Z"))
  private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
  private val container = AppContainer(context, clock = clock, database = database)
  private val taskService = container.taskService
  private val alarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
  private val notificationManager = shadowOf(context.getSystemService(NotificationManager::class.java))

  @Before
  fun allowExactAlarms() {
    ShadowAlarmManager.setCanScheduleExactAlarms(true)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun savingATask_setsOneExactAlarmAtItsFireTime_thatFiresInDoze() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))

    val alarm = alarmManager.scheduledAlarms.single()
    assertEquals(Instant.parse("2026-10-24T13:00:00Z").toEpochMilli(), alarm.triggerAtMs)
    assertEquals(AlarmManager.RTC_WAKEUP, alarm.type)
    assertEquals(ShadowAlarmManager.WINDOW_EXACT, alarm.windowLengthMs)
    assertTrue(alarm.isAllowWhileIdle)
  })

  @Test
  fun editingTheSoonestTaskLater_movesTheAlarmToTheNextSoonest() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-24T15:00:00Z"))

    taskService.update(medsId, title = "Take meds", scheduledTime = Instant.parse("2026-10-24T16:00:00Z"))

    assertEquals(listOf(Instant.parse("2026-10-24T15:00:00Z")), alarmTimes())
  })

  @Test
  fun deletingTheOnlyTask_leavesNoAlarm() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))

    taskService.delete(medsId)

    assertEquals(emptyList<Instant>(), alarmTimes())
  })

  @Test
  fun twoTasksAtTheSameTime_shareOneAlarm_andWhenItFiresEachGetsAnOngoingNotificationWithDone() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    assertEquals(listOf(Instant.parse("2026-10-24T13:00:00Z")), alarmTimes())

    fireTheAlarm()

    val reminders = notificationManager.allNotifications
    assertEquals(setOf("Take meds", "Call dentist"), reminders.map(::titleOf).toSet())
    for (reminder in reminders) {
      val ongoingFlag = reminder.flags and Notification.FLAG_ONGOING_EVENT
      assertTrue(ongoingFlag != 0)
      assertEquals(listOf("Done"), reminder.actions.map({ action -> action.title.toString() }))
    }
  })

  @Test
  fun firingTheAlarm_remindsOnlyTheDueTask_andSetsTheAlarmForTheNextOne() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-24T14:00:00Z"))

    fireTheAlarm()

    assertEquals(listOf("Take meds"), notificationManager.allNotifications.map(::titleOf))
    assertEquals(listOf(Instant.parse("2026-10-24T14:00:00Z")), alarmTimes())
  })

  @Test
  fun done_completesTheTask_clearsItsReminder_andTheTaskLeavesTheList() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()

    taskService.complete(medsId)

    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
    assertEquals(emptyList<Task>(), taskService.observeTaskList().first())
    assertEquals(clock.now, taskService.findTask(medsId)?.completedAt)
  })

  @Test
  fun snoozingAShowingReminder_clearsIt_recordsTheSnooze_andMovesTheAlarm() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()

    taskService.snooze(medsId, SnoozeDuration.THIRTY_MINUTES)

    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
    val snoozed = taskService.findTask(medsId)!!
    assertEquals(TaskStatus.SNOOZED, snoozed.status)
    assertEquals(1, snoozed.snoozeCount)
    assertEquals(Instant.parse("2026-10-24T13:30:00Z"), snoozed.snoozedUntil)
    assertEquals(listOf(Instant.parse("2026-10-24T13:30:00Z")), alarmTimes())
  })

  @Test
  fun whenTheSnoozeRunsOut_theSameReminderAppearsAgain() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    taskService.snooze(medsId, SnoozeDuration.FIFTEEN_MINUTES)

    fireTheAlarm()

    assertEquals(Instant.parse("2026-10-24T13:15:00Z"), clock.now)
    assertEquals(listOf("Take meds"), notificationManager.allNotifications.map(::titleOf))
  })

  @Test
  fun editingASnoozedTask_endsTheSnooze_soItFiresAtTheNewTime() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    taskService.snooze(medsId, SnoozeDuration.ONE_DAY)

    taskService.update(medsId, title = "Take meds", scheduledTime = Instant.parse("2026-10-24T18:00:00Z"))

    assertEquals(listOf(Instant.parse("2026-10-24T18:00:00Z")), alarmTimes())
    assertEquals(TaskStatus.PENDING, taskService.findTask(medsId)?.status)
  })

  @Test
  fun withoutTheExactAlarmPermission_savingStillSetsAnInexactAlarmThatFiresInDoze() = runTest(testBody = { ->
    ShadowAlarmManager.setCanScheduleExactAlarms(false)

    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))

    val alarm = alarmManager.scheduledAlarms.single()
    assertEquals(Instant.parse("2026-10-24T13:00:00Z").toEpochMilli(), alarm.triggerAtMs)
    assertNotEquals(ShadowAlarmManager.WINDOW_EXACT, alarm.windowLengthMs)
    assertTrue(alarm.isAllowWhileIdle)
  })

  @Test
  fun appStartup_createsAnAlarmImportanceChannelWithSoundAndVibration() {
    val channel = context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL_ID)

    assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
    assertEquals(AudioAttributes.USAGE_ALARM, channel.audioAttributes.usage)
    assertTrue(channel.sound != null)
    assertTrue(channel.shouldVibrate())
  }

  private fun alarmTimes(): List<Instant> = alarmManager.scheduledAlarms.map({ alarm -> Instant.ofEpochMilli(alarm.triggerAtMs) })

  /** Moves the clock to the alarm's time and does what ReminderAlarmReceiver does when Android delivers it. */
  private suspend fun fireTheAlarm() {
    val alarm = alarmManager.scheduledAlarms.single()
    clock.now = Instant.ofEpochMilli(alarm.triggerAtMs)
    container.reminderSync.sync()
  }
}

private fun titleOf(notification: Notification): String = notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
