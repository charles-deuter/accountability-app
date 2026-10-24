package dev.accountabilityapp.reminders

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.AppContainer
import dev.accountabilityapp.SettableClock
import dev.accountabilityapp.data.AppDatabase
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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

  private fun alarmTimes(): List<Instant> = alarmManager.scheduledAlarms.map({ alarm -> Instant.ofEpochMilli(alarm.triggerAtMs) })
}
