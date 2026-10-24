package dev.accountabilityapp.ui.snooze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.accountabilityapp.R
import dev.accountabilityapp.data.SnoozeDuration
import dev.accountabilityapp.theme.AccountabilityAppTheme

@Composable
fun SnoozeRoute(viewModel: SnoozeViewModel, onFinished: () -> Unit) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(state.isFinished, block = { -> if (state.isFinished) onFinished() })
  SnoozeScreen(onSnooze = viewModel::snooze)
}

/** The five presets, as big buttons on a card over a dimmed scrim. */
@Composable
fun SnoozeScreen(onSnooze: (SnoozeDuration) -> Unit) {
  Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
    Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.padding(24.dp).fillMaxWidth()) {
      Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.snooze_for), style = MaterialTheme.typography.headlineSmall)
        for (duration in SnoozeDuration.entries) {
          Button(onClick = { -> onSnooze(duration) }, modifier = Modifier.fillMaxWidth()) { Text(labelOf(duration)) }
        }
      }
    }
  }
}

@Composable
private fun labelOf(duration: SnoozeDuration): String {
  val labelResource =
    when (duration) {
      SnoozeDuration.FIFTEEN_MINUTES -> R.string.snooze_15_minutes
      SnoozeDuration.THIRTY_MINUTES -> R.string.snooze_30_minutes
      SnoozeDuration.ONE_HOUR -> R.string.snooze_1_hour
      SnoozeDuration.TWO_HOURS -> R.string.snooze_2_hours
      SnoozeDuration.ONE_DAY -> R.string.snooze_1_day
    }
  return stringResource(labelResource)
}

@Preview
@Composable
private fun SnoozeScreenPreview() {
  AccountabilityAppTheme { SnoozeScreen(onSnooze = { _ -> }) }
}
