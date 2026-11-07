package dev.accountabilityapp.ui.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.accountabilityapp.R
import dev.accountabilityapp.theme.AccountabilityAppTheme

@Composable
fun ReminderRoute(viewModel: ReminderViewModel, onSnooze: () -> Unit, onFinished: () -> Unit) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(state.isFinished, block = { -> if (state.isFinished) onFinished() })
  ReminderScreen(title = state.title, onDone = viewModel::complete, onSnooze = onSnooze)
}

/** Full screen, like an incoming call: the Task's title and the only two ways out, which a long title can't push away. */
@Composable
fun ReminderScreen(title: String, onDone: () -> Unit, onSnooze: () -> Unit) {
  Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primaryContainer) {
    Column(
      Modifier.fillMaxSize().safeDrawingPadding().padding(32.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(stringResource(R.string.reminder_label), style = MaterialTheme.typography.titleMedium)
      // The weight measures the title last, so it gets only the room the buttons leave.
      Text(
        title,
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false),
      )
      Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Text(stringResource(R.string.done), style = MaterialTheme.typography.titleLarge)
      }
      OutlinedButton(onClick = onSnooze, modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Text(stringResource(R.string.snooze), style = MaterialTheme.typography.titleLarge)
      }
    }
  }
}

@Preview
@Composable
private fun ReminderScreenPreview() {
  AccountabilityAppTheme { ReminderScreen(title = "Take meds", onDone = { -> }, onSnooze = { -> }) }
}
