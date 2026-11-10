package dev.accountabilityapp.permissions

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Opens the system screen where the user allows [grant] for this app. */
// BatteryLife: Play allows asking for the exemption directly when the app's core function needs it, and on-time
// Reminders are this app's core function (PRD risk #3; ticket 21 makes the case in the Play Console declaration).
@SuppressLint("BatteryLife", "InlinedApi")
fun settingsIntentOf(grant: Grant, packageName: String): Intent {
  val packageUri = Uri.parse("package:$packageName")
  return when (grant) {
    Grant.NOTIFICATIONS -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    Grant.FULL_SCREEN_INTENT -> Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri)
    Grant.EXACT_ALARMS -> Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)
    // A one-tap system dialog rather than the full battery settings list.
    Grant.BATTERY_EXEMPTION -> Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri)
  }
}
