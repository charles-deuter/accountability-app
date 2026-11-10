package dev.accountabilityapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

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

fun templateOf(title: String, recurrence: Recurrence, timeOfDay: LocalTime, isActive: Boolean, createdAt: Instant): Template =
  when (recurrence) {
    Recurrence.Daily -> Template(title = title, recurrenceType = RecurrenceType.DAILY, recurrenceData = 0, timeOfDay = timeOfDay, isActive = isActive, createdAt = createdAt)
    is Recurrence.Weekly ->
      Template(title = title, recurrenceType = RecurrenceType.WEEKLY, recurrenceData = weekdayBitmaskOf(recurrence.days), timeOfDay = timeOfDay, isActive = isActive, createdAt = createdAt)
  }

fun recurrenceOf(template: Template): Recurrence =
  when (template.recurrenceType) {
    RecurrenceType.DAILY -> Recurrence.Daily
    RecurrenceType.WEEKLY -> Recurrence.Weekly(weekdaysOf(template.recurrenceData))
  }

private fun weekdayBitmaskOf(days: Set<DayOfWeek>): Int = days.sumOf({ day -> bitOf(day) })

private fun weekdaysOf(bitmask: Int): Set<DayOfWeek> = DayOfWeek.entries.filter({ day -> bitmask and bitOf(day) != 0 }).toSet()

private fun bitOf(day: DayOfWeek): Int = 1 shl (day.value - 1)
