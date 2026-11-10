package dev.accountabilityapp.ui.onboarding

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.accountabilityapp.permissions.FakePermissionSource
import dev.accountabilityapp.permissions.Grant
import dev.accountabilityapp.permissions.PermissionSource
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity

@RunWith(AndroidJUnit4::class)
class OnboardingTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  @Config(sdk = [31])
  fun onAndroid12_onlyTheExactAlarmAndBatterySteps_appear() {
    showOnboarding(FakePermissionSource())

    assertEquals(listOf(EXACT_ALARMS_BUTTON, BATTERY_BUTTON), stepButtonsShown())
  }

  @Test
  @Config(sdk = [34])
  fun onAndroid14_theSettingsSteps_eachOpenTheirScreenInSettings() {
    showOnboarding(FakePermissionSource())
    val packageUri = "package:dev.accountabilityapp"
    val expectedScreens =
      listOf(
        FULL_SCREEN_BUTTON to SettingsScreen("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT", data = packageUri),
        EXACT_ALARMS_BUTTON to SettingsScreen("android.settings.REQUEST_SCHEDULE_EXACT_ALARM", data = packageUri),
        BATTERY_BUTTON to SettingsScreen("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", data = packageUri),
      )

    for ((button, expectedScreen) in expectedScreens) {
      composeTestRule.onNodeWithText(button).performScrollTo().performClick()
      val started = shadowOf(composeTestRule.activity).nextStartedActivity
      assertEquals(button, expectedScreen, settingsScreenOf(started))
    }
  }

  @Test
  @Config(sdk = [33])
  fun onAndroid13_theNotificationsStep_asksInOneTap_andAnswerNoLeavesTheUserOnOnboarding() {
    showOnboarding(FakePermissionSource())

    composeTestRule.onNodeWithText(NOTIFICATIONS_BUTTON).performScrollTo().performClick()
    val request = shadowOf(composeTestRule.activity).lastRequestedPermission
    // The user saw the dialog and said no, so Android would show it again next time.
    shadowOf(composeTestRule.activity.packageManager).setShouldShowRequestPermissionRationale(POST_NOTIFICATIONS, true)
    answerPermissionRequest(request, PackageManager.PERMISSION_DENIED)

    assertEquals(listOf(POST_NOTIFICATIONS), request?.requestedPermissions?.toList())
    assertEquals(emptyList<SettingsScreen>(), settingsScreensOpened())
  }

  @Test
  @Config(sdk = [33])
  fun whenAndroidWontAskForNotificationsAgain_theNotificationsStep_opensNotificationSettings() {
    showOnboarding(FakePermissionSource())

    composeTestRule.onNodeWithText(NOTIFICATIONS_BUTTON).performScrollTo().performClick()
    // After two refusals Android answers no at once, without a dialog, and stops offering a rationale.
    answerPermissionRequest(shadowOf(composeTestRule.activity).lastRequestedPermission, PackageManager.PERMISSION_DENIED)

    val expectedScreen = SettingsScreen("android.settings.APP_NOTIFICATION_SETTINGS", data = null, appPackage = "dev.accountabilityapp")
    assertEquals(listOf(expectedScreen), settingsScreensOpened())
  }

  @Test
  @Config(sdk = [34])
  fun aStepTheUserAllowedInSettings_showsAsDone_whenTheAppComesBack() {
    val permissionSource = FakePermissionSource()
    showOnboarding(permissionSource)

    composeTestRule.onNodeWithText(EXACT_ALARMS_BUTTON).performScrollTo().performClick()
    leaveForSettings()
    permissionSource.granted.add(Grant.EXACT_ALARMS)
    returnFromSettings()

    composeTestRule.onNodeWithText("Exact alarms allowed").assertExists()
    composeTestRule.onNodeWithText(EXACT_ALARMS_BUTTON).assertDoesNotExist()
  }

  private fun answerPermissionRequest(request: ShadowActivity.PermissionsRequest?, result: Int) {
    val requestedPermissions = request?.requestedPermissions ?: emptyArray()
    val results = IntArray(requestedPermissions.size, init = { _ -> result })
    @Suppress("DEPRECATION") // How Android delivers the answer; the Activity Result API reads it from here.
    composeTestRule.activity.onRequestPermissionsResult(request?.requestCode ?: -1, requestedPermissions, results)
    composeTestRule.waitForIdle()
  }

  /** Every Settings screen the app has opened so far, leaving out the permission dialog. */
  private fun settingsScreensOpened(): List<SettingsScreen> {
    val activity = shadowOf(composeTestRule.activity)
    val started = generateSequence({ -> activity.nextStartedActivity }).toList()
    val settings = started.filter({ intent -> intent.action?.startsWith("android.settings.") == true })
    return settings.map(::settingsScreenOf)
  }

  private fun leaveForSettings() {
    composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
  }

  private fun returnFromSettings() {
    composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
    composeTestRule.waitForIdle()
  }

  private fun showOnboarding(permissionSource: PermissionSource, onFinish: () -> Unit = { -> }) {
    composeTestRule.setContent { OnboardingRoute(onFinish = onFinish, permissionSource = permissionSource) }
  }

  private fun stepButtonsShown(): List<String> =
    ALL_STEP_BUTTONS.filter({ label -> composeTestRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty() })
}

/** The parts of an Intent that pick a Settings screen and the app it shows. */
private data class SettingsScreen(val action: String?, val data: String?, val appPackage: String? = null)

private fun settingsScreenOf(intent: Intent?): SettingsScreen =
  SettingsScreen(intent?.action, intent?.dataString, intent?.getStringExtra("android.provider.extra.APP_PACKAGE"))

private const val NOTIFICATIONS_BUTTON = "Allow notifications"
private const val FULL_SCREEN_BUTTON = "Allow full-screen reminders"
private const val EXACT_ALARMS_BUTTON = "Allow exact alarms"
private const val BATTERY_BUTTON = "Turn off battery optimization"
private val ALL_STEP_BUTTONS = listOf(NOTIFICATIONS_BUTTON, FULL_SCREEN_BUTTON, EXACT_ALARMS_BUTTON, BATTERY_BUTTON)
