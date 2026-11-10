package dev.accountabilityapp.permissions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import dev.accountabilityapp.reminders.canScheduleExactAlarms
import dev.accountabilityapp.reminders.isNotificationPermissionMissing

/**
 * Reads each [Grant] from the system. A grant that predates the device's Android version reads as allowed, as it is by
 * default there.
 */
class AndroidPermissionSource(private val context: Context) : PermissionSource {

  override fun isGranted(grant: Grant): Boolean =
    when (grant) {
      Grant.NOTIFICATIONS -> !isNotificationPermissionMissing(context)
      Grant.FULL_SCREEN_INTENT -> canUseFullScreenIntent(context)
      Grant.EXACT_ALARMS -> canScheduleExactAlarms(context.getSystemService(AlarmManager::class.java))
      Grant.BATTERY_EXEMPTION -> isIgnoringBatteryOptimizations(context)
    }
}

private fun canUseFullScreenIntent(context: Context): Boolean {
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
  val notificationManager = context.getSystemService(NotificationManager::class.java)
  return notificationManager.canUseFullScreenIntent()
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
  val powerManager = context.getSystemService(PowerManager::class.java)
  return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}
