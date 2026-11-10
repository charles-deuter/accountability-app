package dev.accountabilityapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * A recurring series. While it is active and has a next occurrence, it keeps exactly one pending Instance: a Task whose
 * templateId points here. See CONTEXT.md.
 */
@Entity(tableName = "templates")
data class Template(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val recurrenceType: RecurrenceType,
  /** Weekly: the days as a bitmask, Monday the lowest bit. Read it through [recurrenceOf]. */
  val recurrenceData: Int,
  /** Wall-clock time in the phone's current zone. */
  val timeOfDay: LocalTime,
  val isActive: Boolean,
  val createdAt: Instant,
)

enum class RecurrenceType {
  DAILY,
  WEEKLY,
}

fun templateOf(title: String, recurrence: Recurrence, timeOfDay: LocalTime, isActive: Boolean, createdAt: Instant): Template {
  val recurrenceType =
    when (recurrence) {
      Recurrence.Daily -> RecurrenceType.DAILY
      is Recurrence.Weekly -> RecurrenceType.WEEKLY
    }
  val recurrenceData =
    when (recurrence) {
      Recurrence.Daily -> 0
      is Recurrence.Weekly -> weekdayBitmaskOf(recurrence.days)
    }
  return Template(
    title = title,
    recurrenceType = recurrenceType,
    recurrenceData = recurrenceData,
    timeOfDay = timeOfDay,
    isActive = isActive,
    createdAt = createdAt,
  )
}

fun recurrenceOf(template: Template): Recurrence =
  when (template.recurrenceType) {
    RecurrenceType.DAILY -> Recurrence.Daily
    RecurrenceType.WEEKLY -> Recurrence.Weekly(weekdaysOf(template.recurrenceData))
  }

/** When an active Template's first Instance fires if it is saved at [now]: the first occurrence still ahead. Null while paused. */
fun firstInstanceTimeOf(template: Template, now: Instant, zone: ZoneId): Instant? {
  if (!template.isActive) return null
  return nextOccurrenceOf(recurrenceOf(template), template.timeOfDay, after = now, zone = zone)
}

/**
 * When the Instance after [instance] fires. It counts from the Instance's series slot (its original time), or from [now]
 * if that has passed, so snoozing, moving, or completing it late or early never shifts the series, and missed occurrences
 * are skipped.
 */
fun nextInstanceTimeOf(template: Template, instance: Task, now: Instant, zone: ZoneId): Instant? {
  val seriesSlot = instance.originalScheduledTime
  val after = maxOf(seriesSlot, now)
  return firstInstanceTimeOf(template, after, zone)
}

private fun weekdayBitmaskOf(days: Set<DayOfWeek>): Int = days.sumOf({ day -> bitOf(day) })

private fun weekdaysOf(bitmask: Int): Set<DayOfWeek> = DayOfWeek.entries.filter({ day -> isDayInBitmask(day, bitmask) }).toSet()

private fun isDayInBitmask(day: DayOfWeek, bitmask: Int): Boolean {
  val dayBit = bitmask and bitOf(day)
  return dayBit != 0
}

private fun bitOf(day: DayOfWeek): Int = 1 shl (day.value - 1)
