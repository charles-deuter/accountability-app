package dev.accountabilityapp.data

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/** The five ways to Snooze a Reminder. */
enum class SnoozeDuration(val length: Duration) {
  FIFTEEN_MINUTES(15.minutes),
  THIRTY_MINUTES(30.minutes),
  ONE_HOUR(1.hours),
  TWO_HOURS(2.hours),
  ONE_DAY(1.days),
}

/** The new Fire time when a Task is Snoozed [now] for [duration]. One day is 24 hours, not "same time tomorrow". */
fun snoozedUntilOf(duration: SnoozeDuration, now: Instant): Instant = now.plus(duration.length.toJavaDuration())
