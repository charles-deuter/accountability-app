package dev.accountabilityapp.permissions

import android.os.Build

/**
 * A permission or setting the user must allow for Reminders to ring on time. [sinceSdkLevel] is the first Android version
 * that asks for it; on earlier versions it is allowed by default.
 */
enum class Grant(val sinceSdkLevel: Int) {
  NOTIFICATIONS(Build.VERSION_CODES.TIRAMISU),
  FULL_SCREEN_INTENT(Build.VERSION_CODES.UPSIDE_DOWN_CAKE),
  EXACT_ALARMS(Build.VERSION_CODES.S),
  BATTERY_EXEMPTION(Build.VERSION_CODES.BASE),
}

/** Answers whether the user has allowed a [Grant]. Asked only about grants that apply to the device's Android version. */
interface PermissionSource {
  fun isGranted(grant: Grant): Boolean
}

/** The grants that the device's Android version asks the user for, in the order onboarding walks through them. */
fun applicableGrantsOf(sdkLevel: Int): List<Grant> = Grant.entries.filter({ grant -> sdkLevel >= grant.sinceSdkLevel })

fun missingGrantsFrom(permissionSource: PermissionSource, sdkLevel: Int): List<Grant> =
  applicableGrantsOf(sdkLevel).filter({ grant -> !permissionSource.isGranted(grant) })
