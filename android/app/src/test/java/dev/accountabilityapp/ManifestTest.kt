package dev.accountabilityapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.ui.reminder.ReminderActivity
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
