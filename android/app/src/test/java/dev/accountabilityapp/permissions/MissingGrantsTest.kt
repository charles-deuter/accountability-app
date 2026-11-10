package dev.accountabilityapp.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class MissingGrantsTest {

  @Test
  fun onAndroid8_withNothingGranted_onlyTheBatteryExemptionIsMissing() {
    val missing = missingGrantsFrom(FakePermissionSource(), sdkLevel = 26)

    assertEquals(listOf(Grant.BATTERY_EXEMPTION), missing)
  }

  @Test
  fun exactAlarms_countFromAndroid12() {
    val missingOnAndroid11 = missingGrantsFrom(FakePermissionSource(), sdkLevel = 30)
    val missingOnAndroid12 = missingGrantsFrom(FakePermissionSource(), sdkLevel = 31)

    assertEquals(listOf(Grant.BATTERY_EXEMPTION), missingOnAndroid11)
    assertEquals(listOf(Grant.EXACT_ALARMS, Grant.BATTERY_EXEMPTION), missingOnAndroid12)
  }

  @Test
  fun notifications_countFromAndroid13() {
    val missingOnAndroid12L = missingGrantsFrom(FakePermissionSource(), sdkLevel = 32)
    val missingOnAndroid13 = missingGrantsFrom(FakePermissionSource(), sdkLevel = 33)

    assertEquals(listOf(Grant.EXACT_ALARMS, Grant.BATTERY_EXEMPTION), missingOnAndroid12L)
    assertEquals(listOf(Grant.NOTIFICATIONS, Grant.EXACT_ALARMS, Grant.BATTERY_EXEMPTION), missingOnAndroid13)
  }

  @Test
  fun theFullScreenIntent_countsFromAndroid14() {
    val missingOnAndroid13 = missingGrantsFrom(FakePermissionSource(), sdkLevel = 33)
    val missingOnAndroid14 = missingGrantsFrom(FakePermissionSource(), sdkLevel = 34)

    assertEquals(listOf(Grant.NOTIFICATIONS, Grant.EXACT_ALARMS, Grant.BATTERY_EXEMPTION), missingOnAndroid13)
    assertEquals(
      listOf(Grant.NOTIFICATIONS, Grant.FULL_SCREEN_INTENT, Grant.EXACT_ALARMS, Grant.BATTERY_EXEMPTION),
      missingOnAndroid14,
    )
  }

  @Test
  fun aGrantTheUserAllowed_isNotMissing() {
    val permissionSource = FakePermissionSource(Grant.NOTIFICATIONS, Grant.BATTERY_EXEMPTION)

    val missing = missingGrantsFrom(permissionSource, sdkLevel = 34)

    assertEquals(listOf(Grant.FULL_SCREEN_INTENT, Grant.EXACT_ALARMS), missing)
  }
}
