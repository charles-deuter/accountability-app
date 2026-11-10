package dev.accountabilityapp

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * The system clock in the phone's current time zone. `Clock.systemDefaultZone()` keeps the zone it started with, so
 * after the user changes zone, Templates would place new Instances by the old one's wall clock. Android resets the
 * default zone when the phone's zone changes, so reading it on every call follows the phone.
 */
class PhoneClock : Clock() {
  override fun instant(): Instant = Instant.now()

  override fun getZone(): ZoneId = ZoneId.systemDefault()

  override fun withZone(zone: ZoneId): Clock = Clock.system(zone)
}
