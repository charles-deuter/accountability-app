package dev.accountabilityapp

import java.time.ZoneId
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneClockTest {

  private val originalZone = TimeZone.getDefault()

  @After
  fun restoreZone() {
    TimeZone.setDefault(originalZone)
  }

  @Test
  fun theZone_followsThePhoneWhenItChanges() {
    val clock = PhoneClock()
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
    val before = clock.zone

    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))

    assertEquals(ZoneId.of("America/New_York"), before)
    assertEquals(ZoneId.of("America/Los_Angeles"), clock.zone)
  }
}
