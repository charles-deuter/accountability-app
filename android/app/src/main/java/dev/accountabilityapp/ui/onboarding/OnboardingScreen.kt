package dev.accountabilityapp.ui.onboarding

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.accountabilityapp.R
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.permissions.Grant
import dev.accountabilityapp.permissions.PermissionSource
import dev.accountabilityapp.permissions.applicableGrantsOf
import dev.accountabilityapp.permissions.settingsIntentOf
import dev.accountabilityapp.theme.AccountabilityAppTheme

/** One grant on the onboarding screen, and whether the user has allowed it yet. */
data class OnboardingStep(val grant: Grant, val isGranted: Boolean)

@Composable
fun OnboardingRoute(
  onFinish: () -> Unit,
  modifier: Modifier = Modifier,
  permissionSource: PermissionSource = appContainerFrom(LocalContext.current).permissionSource,
  sdkLevel: Int = Build.VERSION.SDK_INT,
) {
  val context = LocalContext.current
  var steps by remember({ -> mutableStateOf(onboardingStepsFrom(permissionSource, sdkLevel)) })
  // The user allows grants in Settings, outside the app, so re-check each time the app comes back to the foreground.
  LifecycleResumeEffect(permissionSource, sdkLevel, effects = { ->
    steps = onboardingStepsFrom(permissionSource, sdkLevel)
    this.onPauseOrDispose({ -> })
  })
  val openSettings: (Grant) -> Unit = { grant -> context.startActivity(settingsIntentOf(grant, context.packageName)) }

  OnboardingScreen(steps = steps, onOpenSettings = openSettings, onFinish = onFinish, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
  steps: List<OnboardingStep>,
  onOpenSettings: (Grant) -> Unit,
  onFinish: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.onboarding_title)) }) }) { innerPadding ->
    Column(
      modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(stringResource(R.string.onboarding_intro), style = MaterialTheme.typography.bodyLarge)
      for (step in steps) {
        StepCard(step, onOpenSettings = { -> onOpenSettings(step.grant) })
      }
      Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.onboarding_continue)) }
    }
  }
}

@Composable
private fun StepCard(step: OnboardingStep, onOpenSettings: () -> Unit) {
  val text = stepTextOf(step.grant)
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(stringResource(text.title), style = MaterialTheme.typography.titleMedium)
      Text(stringResource(text.explanation), style = MaterialTheme.typography.bodyMedium)
      if (step.isGranted) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text(stringResource(text.done), color = MaterialTheme.colorScheme.primary)
        }
      } else {
        OutlinedButton(onClick = onOpenSettings) { Text(stringResource(text.action)) }
      }
    }
  }
}

private fun onboardingStepsFrom(permissionSource: PermissionSource, sdkLevel: Int): List<OnboardingStep> =
  applicableGrantsOf(sdkLevel).map({ grant -> OnboardingStep(grant, permissionSource.isGranted(grant)) })

private data class StepText(
  @StringRes val title: Int,
  @StringRes val explanation: Int,
  @StringRes val action: Int,
  @StringRes val done: Int,
)

private fun stepTextOf(grant: Grant): StepText =
  when (grant) {
    Grant.NOTIFICATIONS ->
      StepText(
        R.string.notifications_step_title,
        R.string.notifications_step_explanation,
        R.string.notifications_step_action,
        R.string.notifications_step_done,
      )
    Grant.FULL_SCREEN_INTENT ->
      StepText(
        R.string.full_screen_step_title,
        R.string.full_screen_step_explanation,
        R.string.full_screen_step_action,
        R.string.full_screen_step_done,
      )
    Grant.EXACT_ALARMS ->
      StepText(
        R.string.exact_alarms_step_title,
        R.string.exact_alarms_step_explanation,
        R.string.exact_alarms_step_action,
        R.string.exact_alarms_step_done,
      )
    Grant.BATTERY_EXEMPTION ->
      StepText(
        R.string.battery_step_title,
        R.string.battery_step_explanation,
        R.string.battery_step_action,
        R.string.battery_step_done,
      )
  }

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
  AccountabilityAppTheme {
    OnboardingScreen(
      steps =
        listOf(
          OnboardingStep(Grant.NOTIFICATIONS, isGranted = true),
          OnboardingStep(Grant.FULL_SCREEN_INTENT, isGranted = false),
          OnboardingStep(Grant.EXACT_ALARMS, isGranted = false),
          OnboardingStep(Grant.BATTERY_EXEMPTION, isGranted = false),
        ),
      onOpenSettings = { _ -> },
      onFinish = { -> },
    )
  }
}
