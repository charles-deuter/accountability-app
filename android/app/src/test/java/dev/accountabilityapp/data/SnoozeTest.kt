package dev.accountabilityapp.data

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class SnoozeTest {

  private val now = Instant.parse("2026-10-24T15:00:00Z")

  @Test
  fun eachPreset_snoozesUntilThatLongFromNow() {
    val expected =
      mapOf(
        SnoozeDuration.FIFTEEN_MINUTES to Instant.parse("2026-10-24T15:15:00Z"),
        SnoozeDuration.THIRTY_MINUTES to Instant.parse("2026-10-24T15:30:00Z"),
        SnoozeDuration.ONE_HOUR to Instant.parse("2026-10-24T16:00:00Z"),
        SnoozeDuration.TWO_HOURS to Instant.parse("2026-10-24T17:00:00Z"),
        SnoozeDuration.ONE_DAY to Instant.parse("2026-10-25T15:00:00Z"),
      )

    val actual = SnoozeDuration.entries.associateWith({ duration -> snoozedUntilOf(duration, now) })

    assertEquals(expected, actual)
  }
}
