package dev.accountabilityapp

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** A wall clock the test moves by hand, independent of the coroutine scheduler's virtual time. */
class SettableClock(var now: Instant) : Clock() {
  override fun instant(): Instant = now

  override fun getZone(): ZoneId = ZoneOffset.UTC

  override fun withZone(zone: ZoneId?): Clock = this
}
