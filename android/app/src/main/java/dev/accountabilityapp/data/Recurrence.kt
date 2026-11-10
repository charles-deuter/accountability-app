package dev.accountabilityapp.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** A Template's repeat pattern. See CONTEXT.md. */
sealed interface Recurrence {
  data object Daily : Recurrence

  /** Every week on each of [days]. With no days, the series never fires. */
  data class Weekly(val days: Set<DayOfWeek>) : Recurrence
}

/** The first moment strictly after [after] when a series with [recurrence] at [timeOfDay] fires, or null if it never does. */
fun nextOccurrenceOf(recurrence: Recurrence, timeOfDay: LocalTime, after: Instant, zone: ZoneId): Instant? {
  val startDate = after.atZone(zone).toLocalDate()
  // Today plus the seven days after it reach every weekday, even when today's time has already passed.
  for (dayOffset in 0L..7L) {
    val date = startDate.plusDays(dayOffset)
    if (!isOccurrenceDay(recurrence, date)) continue
    val occurrence = ZonedDateTime.of(date, timeOfDay, zone).toInstant()
    if (occurrence.isAfter(after)) return occurrence
  }
  return null
}

private fun isOccurrenceDay(recurrence: Recurrence, date: LocalDate): Boolean =
  when (recurrence) {
    Recurrence.Daily -> true
    is Recurrence.Weekly -> date.dayOfWeek in recurrence.days
  }
