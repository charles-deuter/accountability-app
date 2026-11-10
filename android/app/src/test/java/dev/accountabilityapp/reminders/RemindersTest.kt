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
import dev.accountabilityapp.data.Recurrence
import dev.accountabilityapp.data.Task
import dev.accountabilityapp.data.TaskStatus
import dev.accountabilityapp.ui.reminder.ReminderActivity
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    taskService.reschedule(medsId, title = "Take meds", scheduledTime = Instant.parse("2026-10-24T16:00:00Z"))

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
      assertEquals(listOf("Done", "Snooze"), reminder.actions.map({ action -> action.title.toString() }))
    }
  })

  @Test
  fun firingTheAlarm_remindsOnlyTheDueTask_andSetsTheAlarmForTheNextOne() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-24T14:00:00Z"))

    fireTheAlarm()

    assertEquals(listOf("Take meds"), notificationManager.allNotifications.map(::titleOf))
    assertEquals(listOf(Instant.parse("2026-10-24T13:05:00Z")), alarmTimes())
    fireTheAlarm()
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
  fun doneOnARecurringReminder_clearsIt_andSetsTheAlarmForTheNextInstance() = runTest(testBody = { ->
    taskService.addRecurring(title = "Take meds", recurrence = Recurrence.Daily, timeOfDay = LocalTime.of(13, 0), isActive = true)
    fireTheAlarm()
    val instance = taskService.observeTaskList().first().single()

    taskService.complete(instance.id)

    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
    assertEquals(listOf(Instant.parse("2026-10-25T13:00:00Z")), alarmTimes())
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
    assertEquals(Instant.parse("2026-10-24T13:30:00Z"), snoozed.scheduledTime)
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

    taskService.reschedule(medsId, title = "Take meds", scheduledTime = Instant.parse("2026-10-24T18:00:00Z"))

    assertEquals(listOf(Instant.parse("2026-10-24T18:00:00Z")), alarmTimes())
    assertEquals(TaskStatus.PENDING, taskService.findTask(medsId)?.status)
  })

  @Test
  fun renamingASnoozedTaskPastItsOriginalTime_firesNoReminder_andLeavesTheAlarmAlone() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take mds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    taskService.snooze(medsId, SnoozeDuration.ONE_DAY)

    taskService.rename(medsId, title = "Take meds")

    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
    assertEquals(listOf(Instant.parse("2026-10-25T13:00:00Z")), alarmTimes())
  })

  @Test
  fun aReminder_carriesAFullScreenIntentToTheTakeoverForItsTask() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))

    fireTheAlarm()

    val reminder = notificationManager.allNotifications.single()
    val takeoverIntent = shadowOf(reminder.fullScreenIntent).savedIntent
    assertEquals(ReminderActivity::class.java.name, takeoverIntent.component?.className)
    assertEquals(medsId, takeoverIntent.getLongExtra(EXTRA_TASK_ID, -1))
  })

  @Test
  fun swipingAReminderAway_snoozesItForThirtyMinutes() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    val reminder = notificationManager.allNotifications.single()

    // What Android sends when the user swipes the notification away.
    val swipeIntent = shadowOf(reminder.deleteIntent).savedIntent
    handleReminderAction(swipeIntent, taskService)

    assertEquals(Instant.parse("2026-10-24T13:30:00Z"), taskService.findTask(medsId)?.scheduledTime)
    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
  })

  @Test
  fun silencingAReminder_keepsItInTheShadeWithDoneAndSnooze_butOnTheSilentChannelWithNoTakeover() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()

    taskService.silence(medsId)

    val reminder = notificationManager.allNotifications.single()
    assertEquals(SILENCED_CHANNEL_ID, reminder.channelId)
    assertEquals("Take meds", titleOf(reminder))
    assertEquals(listOf("Done", "Snooze"), reminder.actions.map({ action -> action.title.toString() }))
    assertTrue(reminder.flags and Notification.FLAG_ONGOING_EVENT != 0)
    assertNull(reminder.fullScreenIntent)
    assertEquals(TaskStatus.PENDING, taskService.findTask(medsId)?.status)
  })

  @Test
  fun swipingASilencedReminderAway_stillSnoozesItForThirtyMinutes() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    taskService.silence(medsId)

    val swipeIntent = shadowOf(notificationManager.allNotifications.single().deleteIntent).savedIntent
    handleReminderAction(swipeIntent, taskService)

    assertEquals(Instant.parse("2026-10-24T13:30:00Z"), taskService.findTask(medsId)?.scheduledTime)
    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
  })

  @Test
  fun aSilencedReminder_staysSilentOnALaterSync() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    taskService.silence(medsId)

    container.reminderSync.sync()

    val reminder = notificationManager.allNotifications.single()
    assertEquals(SILENCED_CHANNEL_ID, reminder.channelId)
    assertNull(reminder.fullScreenIntent)
  })

  @Test
  fun aSilencedReminderReturningFromASnooze_ringsNormally() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    taskService.silence(medsId)
    taskService.snooze(medsId, SnoozeDuration.FIFTEEN_MINUTES)

    fireTheAlarm()

    val reminder = notificationManager.allNotifications.single()
    assertEquals(CHANNEL_ID, reminder.channelId)
    assertNotNull(reminder.fullScreenIntent)
  })

  @Test
  fun silencingOneOfTwoShowingReminders_leavesTheOtherRinging() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()

    taskService.silence(medsId)

    val channelByTitle = notificationManager.allNotifications.associate({ reminder -> titleOf(reminder) to reminder.channelId })
    assertEquals(mapOf("Take meds" to SILENCED_CHANNEL_ID, "Call dentist" to CHANNEL_ID), channelByTitle)
  })

  @Test
  fun silencingATaskWithNoShowingReminder_postsNothing() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))

    taskService.silence(medsId)

    assertEquals(emptyList<Notification>(), notificationManager.allNotifications)
  })

  @Test
  fun withNoResponse_aReminderStopsRingingAfterFiveMinutes_andStaysInTheShadeWithDoneAndSnooze() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()
    assertEquals(listOf(Instant.parse("2026-10-24T13:05:00Z")), alarmTimes())

    fireTheAlarm()

    val reminder = notificationManager.allNotifications.single()
    assertEquals(SILENCED_CHANNEL_ID, reminder.channelId)
    assertNull(reminder.fullScreenIntent)
    assertEquals(listOf("Done", "Snooze"), reminder.actions.map({ action -> action.title.toString() }))
    assertEquals(TaskStatus.PENDING, taskService.findTask(medsId)?.status)
    assertEquals(0, taskService.findTask(medsId)?.snoozeCount)
    assertEquals(emptyList<Instant>(), alarmTimes())
  })

  @Test
  fun theFiveMinuteLimit_survivesTheProcessDying() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()

    // A new process: new objects, while the alarm and the notification live on in the system.
    val nextProcess = AppContainer(context, clock = clock, database = database)
    clock.now = Instant.ofEpochMilli(alarmManager.scheduledAlarms.single().triggerAtMs)
    nextProcess.reminderSync.sync()

    assertEquals(SILENCED_CHANNEL_ID, notificationManager.allNotifications.single().channelId)
  })

  @Test
  fun aSyncBeforeTheFiveMinutesAreUp_leavesTheReminderRinging_andKeepsTheAlarmAtTheLimit() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()

    clock.now = Instant.parse("2026-10-24T13:04:59Z")
    container.reminderSync.sync()

    assertEquals(CHANNEL_ID, notificationManager.allNotifications.single().channelId)
    assertEquals(listOf(Instant.parse("2026-10-24T13:05:00Z")), alarmTimes())
  })

  @Test
  fun aFireTimeSoonerThanTheRingLimit_keepsTheAlarm_thenTheLimitFollows() = runTest(testBody = { ->
    taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    taskService.add(title = "Call dentist", scheduledTime = Instant.parse("2026-10-24T13:02:00Z"))
    fireTheAlarm()
    assertEquals(listOf(Instant.parse("2026-10-24T13:02:00Z")), alarmTimes())

    fireTheAlarm()

    assertEquals(listOf(Instant.parse("2026-10-24T13:05:00Z")), alarmTimes())
    fireTheAlarm()
    val channelByTitle = notificationManager.allNotifications.associate({ reminder -> titleOf(reminder) to reminder.channelId })
    assertEquals(mapOf("Take meds" to SILENCED_CHANNEL_ID, "Call dentist" to CHANNEL_ID), channelByTitle)
    assertEquals(listOf(Instant.parse("2026-10-24T13:07:00Z")), alarmTimes())
  })

  @Test
  fun respondingBeforeTheLimit_dropsTheRingLimitAlarm() = runTest(testBody = { ->
    val medsId = taskService.add(title = "Take meds", scheduledTime = Instant.parse("2026-10-24T13:00:00Z"))
    fireTheAlarm()

    taskService.silence(medsId)

    assertEquals(emptyList<Instant>(), alarmTimes())
  })

  @Test
  fun afterARestart_tasksThatCameDueAreReminded_completedOnesNever_andTheAlarmIsSetForTheNext() = runTest(testBody = { ->
    // Written before the phone went off; the reboot erased their alarm and notifications.
    val taskDao = database.taskDao()
    val createdAt = Instant.parse("2026-10-24T08:00:00Z")
    taskDao.insert(Task(title = "Missed while off", scheduledTime = Instant.parse("2026-10-24T11:00:00Z"), createdAt = createdAt))
    taskDao.insert(snoozedTask("Snooze ran out while off", snoozedUntil = Instant.parse("2026-10-24T11:30:00Z")))
    taskDao.insert(snoozedTask("Still snoozed", snoozedUntil = Instant.parse("2026-10-24T13:00:00Z")))
    taskDao.insert(Task(title = "Later", scheduledTime = Instant.parse("2026-10-24T14:00:00Z"), createdAt = createdAt))
    taskDao.insert(
      Task(
        title = "Already done",
        scheduledTime = Instant.parse("2026-10-24T12:30:00Z"),
        status = TaskStatus.COMPLETED,
        completedAt = createdAt,
        createdAt = createdAt,
      )
    )

    container.reminderSync.sync()

    val remindedTitles = notificationManager.allNotifications.map(::titleOf).toSet()
    assertEquals(setOf("Missed while off", "Snooze ran out while off"), remindedTitles)
    assertEquals(listOf(Instant.parse("2026-10-24T12:05:00Z")), alarmTimes())
    fireTheAlarm()
    assertEquals(listOf(Instant.parse("2026-10-24T13:00:00Z")), alarmTimes())
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

  @Test
  fun appStartup_createsASilentChannelForSilencedReminders_thatStillAppearsInTheShade() {
    val channel = context.getSystemService(NotificationManager::class.java).getNotificationChannel(SILENCED_CHANNEL_ID)

    assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
    assertNull(channel.sound)
    assertFalse(channel.shouldVibrate())
  }

  private fun alarmTimes(): List<Instant> = alarmManager.scheduledAlarms.map({ alarm -> Instant.ofEpochMilli(alarm.triggerAtMs) })

  /** Moves the clock to the alarm's time and does what ReminderAlarmReceiver does when Android delivers it. */
  private suspend fun fireTheAlarm() {
    val alarm = alarmManager.scheduledAlarms.single()
    clock.now = Instant.ofEpochMilli(alarm.triggerAtMs)
    container.reminderSync.sync()
  }
}

private fun snoozedTask(title: String, snoozedUntil: Instant): Task =
  Task(
    title = title,
    scheduledTime = snoozedUntil,
    originalScheduledTime = Instant.parse("2026-10-24T10:00:00Z"),
    status = TaskStatus.SNOOZED,
    snoozeCount = 1,
    createdAt = Instant.parse("2026-10-24T08:00:00Z"),
  )

private fun titleOf(notification: Notification): String = notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
