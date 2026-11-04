package dev.accountabilityapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.reminders.RescheduleReceiver
import dev.accountabilityapp.ui.reminder.ReminderActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The manifest entries Reminders depend on. A missing one fails silently on a real phone (no takeover, no Reminders after
 * a reboot), so each is checked here through the package manager.
 */
@RunWith(AndroidJUnit4::class)
class ManifestTest {

  private val context: Context = ApplicationProvider.getApplicationContext()

  @Test
  fun theTakeover_showsOverTheLockScreen_andTurnsTheScreenOn() {
    val takeover = context.packageManager.getActivityInfo(ComponentName(context, ReminderActivity::class.java), 0)

    assertTrue(hasFlag(takeover, hiddenActivityFlagOf("FLAG_SHOW_WHEN_LOCKED")))
    assertTrue(hasFlag(takeover, hiddenActivityFlagOf("FLAG_TURN_SCREEN_ON")))
  }

  @Test
  fun bootClockChangesAndAppUpdates_allReachTheRescheduleReceiver() {
    val systemEvents =
      listOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
      )

    for (action in systemEvents) {
      val intent = Intent(action).setPackage(context.packageName)
      val receivers = context.packageManager.queryBroadcastReceivers(intent, 0)
      val receiverNames = receivers.map({ receiver -> receiver.activityInfo.name })
      assertEquals(action, listOf(RescheduleReceiver::class.java.name), receiverNames)
    }
  }

  @Test
  fun theAppMayRunAtBoot() {
    assertTrue(Manifest.permission.RECEIVE_BOOT_COMPLETED in requestedPermissionsOf(context))
  }

  @Test
  fun theAppAsksForFullScreenIntents() {
    val requestedPermissions = requestedPermissionsOf(context)

    assertTrue(Manifest.permission.USE_FULL_SCREEN_INTENT in requestedPermissions)
  }
}

private fun hasFlag(activityInfo: ActivityInfo, flag: Int): Boolean = activityInfo.flags and flag != 0

/** These `ActivityInfo` flags are hidden from the SDK but present at runtime, where the manifest attributes set them. */
private fun hiddenActivityFlagOf(name: String): Int = ActivityInfo::class.java.getField(name).getInt(null)

private fun requestedPermissionsOf(context: Context): List<String> {
  val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
  return packageInfo.requestedPermissions?.toList() ?: emptyList()
}
