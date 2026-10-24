package dev.accountabilityapp.reminders

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** True on Android 13+ when the user hasn't allowed notifications yet; without it, Reminders are silently dropped. */
fun isNotificationPermissionMissing(context: Context): Boolean {
  val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
  if (!needsPermission) return false
  val permissionState = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
  return permissionState != PackageManager.PERMISSION_GRANTED
}
