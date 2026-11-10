package dev.accountabilityapp.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 2026-11-10 is a Tuesday. */
class RecurrenceTest {

  private val nineAm = LocalTime.of(9, 0)
  private val mondayWednesdayFriday = Recurrence.Weekly(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))

  @Test
  fun daily_laterToday_firesToday() {
    val next = nextOccurrenceOf(Recurrence.Daily, nineAm, after = Instant.parse("2026-11-10T08:00:00Z"), zone = ZoneOffset.UTC)

    assertEquals(Instant.parse("2026-11-10T09:00:00Z"), next)
  }

  @Test
  fun daily_whenTodaysTimeHasPassed_firesTomorrow() {
    val next = nextOccurrenceOf(Recurrence.Daily, nineAm, after = Instant.parse("2026-11-10T09:30:00Z"), zone = ZoneOffset.UTC)

    assertEquals(Instant.parse("2026-11-11T09:00:00Z"), next)
  }

  @Test
  fun daily_exactlyAtItsTime_firesTomorrow() {
    val next = nextOccurrenceOf(Recurrence.Daily, nineAm, after = Instant.parse("2026-11-10T09:00:00Z"), zone = ZoneOffset.UTC)

    assertEquals(Instant.parse("2026-11-11T09:00:00Z"), next)
  }

  @Test
  fun weekly_onOneOfItsDays_beforeItsTime_firesToday() {
    val tuesdays = Recurrence.Weekly(setOf(DayOfWeek.TUESDAY))

    val next = nextOccurrenceOf(tuesdays, nineAm, after = Instant.parse("2026-11-10T08:00:00Z"), zone = ZoneOffset.UTC)

    assertEquals(Instant.parse("2026-11-10T09:00:00Z"), next)
  }

  @Test
  fun weekly_onOneOfItsDays_afterItsTime_firesNextWeek() {
    val tuesdays = Recurrence.Weekly(setOf(DayOfWeek.TUESDAY))

    val next = nextOccurrenceOf(tuesdays, nineAm, after = Instant.parse("2026-11-10T09:30:00Z"), zone = ZoneOffset.UTC)

    assertEquals(Instant.parse("2026-11-17T09:00:00Z"), next)
  }

  @Test
  fun weekly_skipsToItsNextDay() {
    val next = nextOccurrenceOf(mondayWednesdayFriday, nineAm, after = Instant.parse("2026-11-10T08:00:00Z"), zone = ZoneOffset.UTC)

    assertEquals(Instant.parse("2026-11-11T09:00:00Z"), next)
  }

  @Test
  fun weekly_fromFriday_wrapsToMonday() {
    val fridayAfterNine = Instant.parse("2026-11-13T10:00:00Z")

    val next = nextOccurrenceOf(mondayWednesdayFriday, nineAm, after = fridayAfterNine, zone = ZoneOffset.UTC)

    assertEquals(Instant.parse("2026-11-16T09:00:00Z"), next)
  }

  @Test
  fun weekly_withNoDays_neverFires() {
    val noDays = Recurrence.Weekly(emptySet())

    val next = nextOccurrenceOf(noDays, nineAm, after = Instant.parse("2026-11-10T08:00:00Z"), zone = ZoneOffset.UTC)

    assertNull(next)
  }

  @Test
  fun timeOfDay_isWallClockTimeInTheGivenZone() {
    val newYork = ZoneId.of("America/New_York")

    val next = nextOccurrenceOf(Recurrence.Daily, nineAm, after = Instant.parse("2026-11-10T08:00:00Z"), zone = newYork)

    assertEquals(Instant.parse("2026-11-10T14:00:00Z"), next)
  }
}
